package com.ptit.cinema.service;

import com.ptit.cinema.model.BookingModels.AdminPayment;
import com.ptit.cinema.model.BookingModels.AdminPaymentSeat;
import com.ptit.cinema.model.BookingModels.PaymentSummary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminPaymentService {
    private static final Set<String> STATUSES = Set.of("ALL", "PENDING", "PAID", "EXPIRED", "CANCELLED");
    private final JdbcTemplate jdbc;
    private final BookingService bookings;

    public AdminPaymentService(JdbcTemplate jdbc, BookingService bookings) {
        this.jdbc = jdbc;
        this.bookings = bookings;
    }

    public PaymentSummary summary() {
        bookings.expirePendingTransfers();
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(CASE WHEN TrangThaiThanhToan = 'PAID' THEN ThanhTien ELSE 0 END), 0) AS TongDoanhThu,
                       COALESCE(SUM(CASE WHEN TrangThaiThanhToan = 'PAID'
                                              AND DATE(ThoiGianThanhToan) = CURRENT_DATE()
                                         THEN ThanhTien ELSE 0 END), 0) AS DoanhThuHomNay,
                       SUM(CASE WHEN TrangThaiThanhToan = 'PAID' THEN 1 ELSE 0 END) AS SoGiaoDichDaThanhToan,
                       SUM(CASE WHEN TrangThaiThanhToan = 'PENDING' THEN 1 ELSE 0 END) AS SoDonChoThanhToan,
                       SUM(CASE WHEN TrangThaiThanhToan = 'EXPIRED' THEN 1 ELSE 0 END) AS SoDonHetHan
                FROM HoaDon
                """, (rs, rowNum) -> new PaymentSummary(
                rs.getLong("TongDoanhThu"),
                rs.getLong("DoanhThuHomNay"),
                rs.getLong("SoGiaoDichDaThanhToan"),
                rs.getLong("SoDonChoThanhToan"),
                rs.getLong("SoDonHetHan")
        ));
    }

    @Transactional
    public List<AdminPayment> payments(String status) {
        bookings.expirePendingTransfers();
        String normalized = status == null || status.isBlank() ? "ALL" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) throw new IllegalArgumentException("Bộ lọc trạng thái thanh toán không hợp lệ.");
        List<PaymentHeader> headers = jdbc.query("""
                SELECT HD.MaHoaDon, HD.TrangThaiThanhToan, HD.HinhThucThanhToan, HD.ThanhTien,
                       HD.TaoLuc, HD.ThoiGianThanhToan, HD.HetHanThanhToan,
                       HD.MaThamChieuChuyenKhoan, KH.HoTen AS TenKhachHang, KH.Sdt,
                       Admin.Email AS XacNhanBoi
                FROM HoaDon HD
                LEFT JOIN KhachHang KH ON KH.MaKhachHang = HD.MaKhachHang
                LEFT JOIN TaiKhoan Admin ON Admin.MaTaiKhoan = HD.XacNhanBoi
                WHERE (? = 'ALL' OR HD.TrangThaiThanhToan = ?)
                ORDER BY HD.TaoLuc DESC, HD.MaHoaDon DESC
                LIMIT 200
                """, (rs, rowNum) -> new PaymentHeader(
                rs.getString("MaHoaDon"),
                rs.getString("TrangThaiThanhToan"),
                rs.getString("HinhThucThanhToan"),
                rs.getInt("ThanhTien"),
                rs.getTimestamp("TaoLuc").toLocalDateTime(),
                rs.getTimestamp("ThoiGianThanhToan") == null ? null : rs.getTimestamp("ThoiGianThanhToan").toLocalDateTime(),
                rs.getTimestamp("HetHanThanhToan") == null ? null : rs.getTimestamp("HetHanThanhToan").toLocalDateTime(),
                rs.getString("MaThamChieuChuyenKhoan"),
                rs.getString("TenKhachHang"),
                rs.getString("Sdt"),
                rs.getString("XacNhanBoi")
        ), normalized, normalized);
        return headers.stream().map(header -> new AdminPayment(
                header.invoiceId(), header.status(), header.method(), header.amount(),
                header.createdAt(), header.paidAt(), header.expiresAt(), header.reference(),
                header.customerName(), header.phone(), header.confirmedBy(), seats(header.invoiceId())
        )).toList();
    }

    @Transactional
    public void confirmTransfer(String invoiceId, String adminId) {
        PaymentHeader header = lockInvoice(invoiceId);
        if (!"PENDING".equals(header.status())) {
            throw new IllegalStateException("Chỉ xác nhận được đơn chuyển khoản đang chờ thanh toán.");
        }
        Boolean notExpired = jdbc.queryForObject("""
                SELECT HetHanThanhToan > CURRENT_TIMESTAMP
                FROM HoaDon
                WHERE MaHoaDon = ?
                """, Boolean.class, invoiceId);
        if (!Boolean.TRUE.equals(notExpired)) {
            throw new IllegalStateException("Đơn đã hết hạn; ghế đang được giải phóng và không thể xác nhận thanh toán.");
        }
        if (!"Chuyển khoản".equals(header.method())) {
            throw new IllegalStateException("Đơn này không phải thanh toán chuyển khoản.");
        }
        String staffId = jdbc.query("""
                SELECT MaNVThuong FROM NhanVienThuong ORDER BY MaNVThuong LIMIT 1
                """, rs -> rs.next() ? rs.getString(1) : null);
        if (staffId == null) throw new IllegalStateException("Cơ sở dữ liệu chưa có nhân viên bán vé để ghi nhận hóa đơn.");

        List<SeatToConfirm> seats = jdbc.query("""
                SELECT C.MaSuatChieu, C.MaGhe, C.MaPhong, C.Gia
                FROM ChiTietGheHoaDon C
                WHERE C.MaHoaDon = ?
                ORDER BY C.MaGhe
                FOR UPDATE
                """, (rs, rowNum) -> new SeatToConfirm(
                rs.getString("MaSuatChieu"),
                rs.getString("MaGhe"),
                rs.getString("MaPhong"),
                rs.getInt("Gia")
        ), invoiceId);
        if (seats.isEmpty()) throw new IllegalStateException("Đơn không có ghế để xác nhận.");
        try {
            for (SeatToConfirm seat : seats) {
                bookings.insertTicket(invoiceId, staffId, seat.showtimeId(), seat.seatId(), seat.roomId(), seat.price());
            }
        } catch (DuplicateKeyException exception) {
            throw new SeatAlreadyBookedException(
                    "Một hoặc nhiều ghế đã được bán sau khi hết hạn đơn. Không xác nhận được; cần hoàn tiền thủ công.",
                    exception);
        }
        jdbc.update("""
                UPDATE HoaDon
                SET TrangThaiThanhToan = 'PAID', ThoiGianThanhToan = CURRENT_TIMESTAMP,
                    XacNhanBoi = ?, XacNhanLuc = CURRENT_TIMESTAMP
                WHERE MaHoaDon = ? AND TrangThaiThanhToan = 'PENDING'
                """, adminId, invoiceId);
        jdbc.update("DELETE FROM GheGiuThanhToan WHERE MaHoaDon = ?", invoiceId);
        jdbc.update("""
                INSERT INTO NhatKyTaiKhoan (MaTaiKhoan, HanhDong, NguoiThucHien)
                VALUES (?, 'PAYMENT_CONFIRMED', ?)
                """, adminId, adminId);
    }

    @Transactional
    public void cancelTransfer(String invoiceId, String adminId) {
        PaymentHeader header = lockInvoice(invoiceId);
        if (!"PENDING".equals(header.status())) {
            throw new IllegalStateException("Chỉ hủy được đơn đang chờ thanh toán.");
        }
        jdbc.update("""
                UPDATE HoaDon
                SET TrangThaiThanhToan = 'CANCELLED', XacNhanBoi = ?, XacNhanLuc = CURRENT_TIMESTAMP
                WHERE MaHoaDon = ? AND TrangThaiThanhToan = 'PENDING'
                """, adminId, invoiceId);
        jdbc.update("DELETE FROM GheGiuThanhToan WHERE MaHoaDon = ?", invoiceId);
        jdbc.update("""
                INSERT INTO NhatKyTaiKhoan (MaTaiKhoan, HanhDong, NguoiThucHien)
                VALUES (?, 'PAYMENT_CANCELLED', ?)
                """, adminId, adminId);
    }

    private PaymentHeader lockInvoice(String invoiceId) {
        return jdbc.query("""
                SELECT MaHoaDon, TrangThaiThanhToan, HinhThucThanhToan, ThanhTien,
                       TaoLuc, ThoiGianThanhToan, HetHanThanhToan,
                       MaThamChieuChuyenKhoan, NULL AS TenKhachHang, NULL AS Sdt, NULL AS XacNhanBoi
                FROM HoaDon WHERE MaHoaDon = ? FOR UPDATE
                """, rs -> {
            if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
            return new PaymentHeader(
                    rs.getString("MaHoaDon"),
                    rs.getString("TrangThaiThanhToan"),
                    rs.getString("HinhThucThanhToan"),
                    rs.getInt("ThanhTien"),
                    rs.getTimestamp("TaoLuc").toLocalDateTime(),
                    rs.getTimestamp("ThoiGianThanhToan") == null ? null : rs.getTimestamp("ThoiGianThanhToan").toLocalDateTime(),
                    rs.getTimestamp("HetHanThanhToan") == null ? null : rs.getTimestamp("HetHanThanhToan").toLocalDateTime(),
                    rs.getString("MaThamChieuChuyenKhoan"),
                    null, null, null
            );
        }, invoiceId);
    }

    private List<AdminPaymentSeat> seats(String invoiceId) {
        return jdbc.query("""
                SELECT P.TenPhim, SC.NgayChieu,
                       TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau,
                       PH.TenPhong, C.MaGhe, C.Gia
                FROM ChiTietGheHoaDon C
                JOIN SuatChieu SC ON SC.MaSuatChieu = C.MaSuatChieu
                JOIN Phim P ON P.MaPhim = SC.MaPhim
                JOIN Phong PH ON PH.MaPhong = C.MaPhong
                WHERE C.MaHoaDon = ?
                ORDER BY C.MaGhe
                """, (rs, rowNum) -> new AdminPaymentSeat(
                rs.getString("TenPhim"),
                rs.getDate("NgayChieu").toLocalDate(),
                rs.getString("GioBatDau"),
                rs.getString("TenPhong"),
                rs.getString("MaGhe"),
                rs.getInt("Gia")
        ), invoiceId);
    }

    private record PaymentHeader(
            String invoiceId,
            String status,
            String method,
            int amount,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime paidAt,
            java.time.LocalDateTime expiresAt,
            String reference,
            String customerName,
            String phone,
            String confirmedBy
    ) {}

    private record SeatToConfirm(String showtimeId, String seatId, String roomId, int price) {}
}
