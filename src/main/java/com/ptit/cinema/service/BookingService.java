package com.ptit.cinema.service;

import com.ptit.cinema.model.BookingModels.BookingReceipt;
import com.ptit.cinema.model.BookingModels.BookingRequest;
import com.ptit.cinema.model.BookingModels.PaymentConfiguration;
import com.ptit.cinema.model.BookingModels.MovieOption;
import com.ptit.cinema.model.BookingModels.ProductOption;
import com.ptit.cinema.model.BookingModels.ProductOrder;
import com.ptit.cinema.model.BookingModels.SeatOption;
import com.ptit.cinema.model.BookingModels.ShowtimeOption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final JdbcTemplate jdbc;
    private final VietQrService vietQr;
    private final int transferExpiryMinutes;

    public BookingService(
            JdbcTemplate jdbc,
            VietQrService vietQr,
            @Value("${app.payment.transfer-expiry-minutes:15}") int transferExpiryMinutes
    ) {
        this.jdbc = jdbc;
        this.vietQr = vietQr;
        if (transferExpiryMinutes < 1 || transferExpiryMinutes > 120) {
            throw new IllegalArgumentException("Thời hạn thanh toán chuyển khoản phải từ 1 đến 120 phút.");
        }
        this.transferExpiryMinutes = transferExpiryMinutes;
    }

    public List<MovieOption> movies() {
        return jdbc.query("""
                SELECT DISTINCT P.MaPhim, P.TenPhim, P.ThoiLuong, P.NgayKhoiChieu
                FROM Phim P
                JOIN SuatChieu SC ON SC.MaPhim = P.MaPhim
                ORDER BY P.TenPhim
                """, (rs, rowNum) -> new MovieOption(
                rs.getString("MaPhim"),
                rs.getString("TenPhim"),
                (Integer) rs.getObject("ThoiLuong"),
                rs.getDate("NgayKhoiChieu") == null ? null : rs.getDate("NgayKhoiChieu").toLocalDate()
        ));
    }

    @Transactional
    public List<ShowtimeOption> showtimes(String movieId) {
        expirePendingTransfers();
        return jdbc.query("""
                SELECT SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                       TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau,
                       TIME_FORMAT(SC.GioKetThuc, '%H:%i') AS GioKetThuc,
                       SC.MaPhong, PH.TenPhong, PH.LoaiPhong, COUNT(DISTINCT G.MaGhe) AS SucChua,
                       COUNT(DISTINCT CASE WHEN V.MaVe IS NOT NULL OR H.MaGhe IS NOT NULL THEN G.MaGhe END) AS SoGheDaDat
                FROM SuatChieu SC
                JOIN Phim P ON P.MaPhim = SC.MaPhim
                JOIN Phong PH ON PH.MaPhong = SC.MaPhong
                LEFT JOIN Ghe G ON G.MaPhong = SC.MaPhong
                LEFT JOIN Ve V ON V.MaSuatChieu = SC.MaSuatChieu
                              AND V.MaGhe = G.MaGhe AND V.MaPhong = G.MaPhong
                LEFT JOIN GheGiuThanhToan H ON H.MaSuatChieu = SC.MaSuatChieu
                              AND H.MaGhe = G.MaGhe AND H.MaPhong = G.MaPhong
                              AND H.HetHanLuc > CURRENT_TIMESTAMP
                WHERE (? IS NULL OR SC.MaPhim = ?)
                GROUP BY SC.MaSuatChieu, SC.MaPhim, P.TenPhim, SC.NgayChieu,
                         SC.GioBatDau, SC.GioKetThuc, SC.MaPhong, PH.TenPhong, PH.LoaiPhong
                ORDER BY SC.NgayChieu, SC.GioBatDau
                """, (rs, rowNum) -> new ShowtimeOption(
                rs.getString("MaSuatChieu"),
                rs.getString("MaPhim"),
                rs.getString("TenPhim"),
                rs.getDate("NgayChieu").toLocalDate(),
                rs.getString("GioBatDau"),
                rs.getString("GioKetThuc"),
                rs.getString("MaPhong"),
                rs.getString("TenPhong"),
                rs.getString("LoaiPhong"),
                rs.getInt("SucChua"),
                rs.getInt("SoGheDaDat")
        ), movieId, movieId);
    }

    @Transactional
    public List<SeatOption> seats(String showtimeId) {
        expirePendingTransfers();
        String roomId = roomForShowtime(showtimeId);
        return jdbc.query("""
                SELECT G.MaGhe, G.HangGhe, G.LoaiGhe, V.MaVe, H.MaHoaDon AS MaGiu,
                       PG.Gia AS GiaVe
                FROM Ghe G
                JOIN CauHinhGiaVe PG ON PG.LoaiGhe =
                    CASE WHEN UPPER(G.LoaiGhe) = 'VIP' THEN 'VIP' ELSE 'THUONG' END
                LEFT JOIN Ve V ON V.MaGhe = G.MaGhe
                              AND V.MaPhong = G.MaPhong
                              AND V.MaSuatChieu = ?
                LEFT JOIN GheGiuThanhToan H ON H.MaGhe = G.MaGhe
                              AND H.MaPhong = G.MaPhong
                              AND H.MaSuatChieu = ?
                              AND H.HetHanLuc > CURRENT_TIMESTAMP
                WHERE G.MaPhong = ?
                ORDER BY G.HangGhe, G.MaGhe
                """, (rs, rowNum) -> {
            String seatType = rs.getString("LoaiGhe");
            return new SeatOption(
                    rs.getString("MaGhe"),
                    rs.getString("HangGhe"),
                    seatType,
                    rs.getString("MaVe") != null || rs.getString("MaGiu") != null,
                    rs.getInt("GiaVe")
            );
        }, showtimeId, showtimeId, roomId);
    }

    public PaymentConfiguration paymentConfiguration() {
        return new PaymentConfiguration(
                vietQr.isAvailable(),
                vietQr.bankName(),
                vietQr.imageUrl(),
                transferExpiryMinutes
        );
    }

    @Transactional
    public void expirePendingTransfers() {
        jdbc.update("""
                UPDATE HoaDon
                SET TrangThaiThanhToan = 'EXPIRED'
                WHERE TrangThaiThanhToan = 'PENDING'
                  AND HetHanThanhToan <= CURRENT_TIMESTAMP
                """);
        jdbc.update("""
                DELETE H FROM GheGiuThanhToan H
                JOIN HoaDon HD ON HD.MaHoaDon = H.MaHoaDon
                WHERE HD.TrangThaiThanhToan <> 'PENDING'
                   OR H.HetHanLuc <= CURRENT_TIMESTAMP
                """);
    }

    public List<ProductOption> products() {
        return jdbc.query("""
                SELECT MaSanPham, TenSanPham, Gia
                FROM SanPham
                ORDER BY TenSanPham
                """, (rs, rowNum) -> new ProductOption(
                rs.getString("MaSanPham"),
                rs.getString("TenSanPham"),
                rs.getInt("Gia")
        ));
    }

    @Transactional
    public BookingReceipt createBooking(BookingRequest request, String accountId) {
        validate(request);
        expirePendingTransfers();
        boolean bankTransfer = "Chuyển khoản".equals(request.hinhThucThanhToan());
        if (bankTransfer && !vietQr.isAvailable()) {
            throw new IllegalStateException("QR chuyển khoản hiện không khả dụng. Vui lòng chọn tiền mặt tại quầy.");
        }
        var showtime = jdbc.query("""
                SELECT SC.MaPhim, P.TenPhim, SC.NgayChieu,
                       TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau,
                       SC.MaPhong, PH.TenPhong
                FROM SuatChieu SC
                JOIN Phim P ON P.MaPhim = SC.MaPhim
                JOIN Phong PH ON PH.MaPhong = SC.MaPhong
                WHERE SC.MaSuatChieu = ?
                FOR UPDATE
                """, (rs, rowNum) -> new ShowtimeOption(
                request.maSuatChieu(),
                rs.getString("MaPhim"),
                rs.getString("TenPhim"),
                rs.getDate("NgayChieu").toLocalDate(),
                rs.getString("GioBatDau"),
                "",
                rs.getString("MaPhong"),
                rs.getString("TenPhong"),
                "",
                0,
                0
        ), request.maSuatChieu()).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy suất chiếu đã chọn."));

        List<String> seats = request.ghe().stream().distinct().sorted().toList();
        if (seats.size() != request.ghe().size()) {
            throw new IllegalArgumentException("Danh sách ghế có mã bị lặp.");
        }
        lockAndValidateSeats(showtime.maPhong(), request.maSuatChieu(), seats);

        int ticketPrice = 0;
        var seatPrices = new java.util.LinkedHashMap<String, Integer>();
        for (String seat : seats) {
            Integer price = jdbc.query("""
                    SELECT PG.Gia
                    FROM Ghe G
                    JOIN CauHinhGiaVe PG ON PG.LoaiGhe =
                        CASE WHEN UPPER(G.LoaiGhe) = 'VIP' THEN 'VIP' ELSE 'THUONG' END
                    WHERE G.MaGhe = ? AND G.MaPhong = ?
                    """, rs -> rs.next() ? rs.getInt(1) : null, seat, showtime.maPhong());
            if (price == null) {
                Integer seatExists = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM Ghe WHERE MaGhe = ? AND MaPhong = ?", Integer.class, seat, showtime.maPhong());
                if (seatExists != null && seatExists > 0) {
                    throw new IllegalStateException("Chưa cấu hình giá vé cho loại ghế trong cơ sở dữ liệu.");
                }
                throw new IllegalArgumentException("Ghế " + seat + " không tồn tại trong phòng chiếu.");
            }
            seatPrices.put(seat, price);
            ticketPrice = Math.addExact(ticketPrice, price);
        }

        long productsPriceTotal = 0;
        List<ProductOrder> orderLines = request.sanPham() == null ? List.of() : request.sanPham();
        Set<String> productIds = new HashSet<>();
        for (ProductOrder line : orderLines) {
            if (line == null || line.maSanPham() == null || line.soLuong() < 1 || line.soLuong() > 20) {
                throw new IllegalArgumentException("Số lượng mỗi món phải từ 1 đến 20.");
            }
            if (!productIds.add(line.maSanPham())) {
                throw new IllegalArgumentException("Mỗi món chỉ được gửi một dòng trong đơn.");
            }
            Integer price = jdbc.query("""
                    SELECT Gia FROM SanPham WHERE MaSanPham = ?
                    """, rs -> rs.next() ? rs.getInt(1) : null, line.maSanPham());
            if (price == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm " + line.maSanPham() + ".");
            }
            productsPriceTotal = Math.addExact(productsPriceTotal, (long) price * line.soLuong());
        }

        long totalAmount = ticketPrice + productsPriceTotal;
        if (totalAmount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Tổng tiền vượt giới hạn có thể thanh toán.");
        }
        int productsPrice = Math.toIntExact(productsPriceTotal);
        int total = Math.toIntExact(totalAmount);
        BookingCustomer customer = jdbc.query("""
                SELECT K.MaKhachHang, K.HoTen, K.Sdt
                FROM TaiKhoan A
                JOIN KhachHang K ON K.MaKhachHang = A.MaKhachHang
                WHERE A.MaTaiKhoan = ? AND A.VaiTro = 'CUSTOMER'
                  AND A.BiKhoa = FALSE
                """, rs -> rs.next() ? new BookingCustomer(
                rs.getString("MaKhachHang"),
                rs.getString("HoTen"),
                rs.getString("Sdt")
        ) : null, accountId);
        if (customer == null) {
            throw new IllegalStateException("Tài khoản khách hàng không hợp lệ.");
        }
        String employeeId = jdbc.query("""
                SELECT MaNVThuong FROM NhanVienThuong ORDER BY MaNVThuong LIMIT 1
                """, rs -> rs.next() ? rs.getString(1) : null);
        if (employeeId == null) {
            throw new IllegalStateException("Cơ sở dữ liệu chưa có nhân viên bán vé để ghi nhận hóa đơn.");
        }

        String invoiceId = "HD" + UUID.randomUUID().toString().replace("-", "");
        String paymentReference = bankTransfer
                ? "PT" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT)
                : null;
        LocalDateTime expiresAt = bankTransfer
                ? LocalDateTime.now().plusMinutes(transferExpiryMinutes).truncatedTo(ChronoUnit.SECONDS)
                : null;
        jdbc.update("""
                INSERT INTO HoaDon
                    (MaHoaDon, HinhThucThanhToan, TrangThaiThanhToan, MaThamChieuChuyenKhoan,
                     ThanhTien, ThoiGianThanhToan, HetHanThanhToan, MaKhachHang)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, invoiceId, request.hinhThucThanhToan(), bankTransfer ? "PENDING" : "PAID",
                paymentReference, total, bankTransfer ? null : LocalDateTime.now(), expiresAt, customer.id());

        for (String seat : seats) {
            try {
                jdbc.update("""
                        INSERT INTO ChiTietGheHoaDon (MaHoaDon, MaSuatChieu, MaGhe, MaPhong, Gia)
                        VALUES (?, ?, ?, ?, ?)
                        """, invoiceId, request.maSuatChieu(), seat, showtime.maPhong(), seatPrices.get(seat));
                if (bankTransfer) {
                    jdbc.update("""
                            INSERT INTO GheGiuThanhToan (MaSuatChieu, MaGhe, MaPhong, MaHoaDon, HetHanLuc)
                            VALUES (?, ?, ?, ?, ?)
                            """, request.maSuatChieu(), seat, showtime.maPhong(), invoiceId, expiresAt);
                } else {
                    insertTicket(invoiceId, employeeId, request.maSuatChieu(), seat, showtime.maPhong(), seatPrices.get(seat));
                }
            } catch (DuplicateKeyException exception) {
                throw new SeatAlreadyBookedException(
                        "Ghế vừa được khách khác đặt. Vui lòng tải lại sơ đồ ghế.", exception);
            }
        }

        for (ProductOrder line : orderLines) {
            jdbc.update("""
                    INSERT INTO BaoGom (MaHoaDon, MaSanPham, SoLuong)
                    VALUES (?, ?, ?)
                    """, invoiceId, line.maSanPham(), line.soLuong());
        }

        return new BookingReceipt(
                invoiceId,
                showtime.tenPhim(),
                showtime.ngayChieu(),
                showtime.gioBatDau(),
                showtime.tenPhong(),
                seats,
                ticketPrice,
                productsPrice,
                total,
                request.hinhThucThanhToan(),
                customer.name(),
                customer.phone(),
                bankTransfer ? "PENDING" : "PAID",
                paymentReference,
                expiresAt,
                bankTransfer ? vietQr.imageUrl() : null,
                bankTransfer ? vietQr.bankName() : null
        );
    }

    void insertTicket(String invoiceId, String employeeId, String showtimeId, String seat, String roomId, int price) {
        jdbc.update("""
                INSERT INTO Ve (MaVe, Gia, MaNVBanve, MaSuatChieu, MaHoaDon, MaGhe, MaPhong)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, "VE" + UUID.randomUUID().toString().replace("-", ""),
                price, employeeId, showtimeId, invoiceId, seat, roomId);
    }

    private String roomForShowtime(String showtimeId) {
        String roomId = jdbc.query("""
                SELECT MaPhong FROM SuatChieu WHERE MaSuatChieu = ?
                """, rs -> rs.next() ? rs.getString(1) : null, showtimeId);
        if (roomId == null) {
            throw new IllegalArgumentException("Không tìm thấy suất chiếu đã chọn.");
        }
        return roomId;
    }

    private void lockAndValidateSeats(String roomId, String showtimeId, List<String> seats) {
        String placeholders = seats.stream().map(ignored -> "?").collect(Collectors.joining(", "));
        List<Object> params = new ArrayList<>();
        params.add(roomId);
        params.addAll(seats);
        String seatLockSql = "SELECT MaGhe FROM Ghe WHERE MaPhong = ? AND MaGhe IN ("
                + placeholders + ") ORDER BY MaGhe FOR UPDATE";
        List<String> existing = jdbc.query(seatLockSql, (rs, rowNum) -> rs.getString(1), params.toArray());
        if (existing.size() != seats.size()) {
            throw new IllegalArgumentException("Một hoặc nhiều ghế không thuộc phòng chiếu của suất này.");
        }

        List<Object> bookedParams = new ArrayList<>();
        bookedParams.add(showtimeId);
        bookedParams.add(roomId);
        bookedParams.addAll(seats);
        String bookedSeatsSql = "SELECT MaGhe FROM Ve WHERE MaSuatChieu = ? AND MaPhong = ? AND MaGhe IN ("
                + placeholders + ")";
        List<String> booked = jdbc.query(bookedSeatsSql, (rs, rowNum) -> rs.getString(1), bookedParams.toArray());
        if (!booked.isEmpty()) {
            throw new IllegalStateException("Ghế " + String.join(", ", booked) + " vừa được đặt. Vui lòng chọn ghế khác.");
        }
        String heldSeatsSql = "SELECT MaGhe FROM GheGiuThanhToan WHERE MaSuatChieu = ? AND MaPhong = ? "
                + "AND HetHanLuc > CURRENT_TIMESTAMP AND MaGhe IN (" + placeholders + ")";
        List<String> held = jdbc.query(heldSeatsSql, (rs, rowNum) -> rs.getString(1), bookedParams.toArray());
        if (!held.isEmpty()) {
            throw new IllegalStateException("Ghế " + String.join(", ", held) + " đang được giữ chỗ chờ thanh toán.");
        }
    }

    private void validate(BookingRequest request) {
        if (request == null || request.maSuatChieu() == null || request.maSuatChieu().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn suất chiếu.");
        }
        if (request.ghe() == null || request.ghe().isEmpty() || request.ghe().size() > 10
                || request.ghe().stream().anyMatch(seat -> seat == null || seat.isBlank())) {
            throw new IllegalArgumentException("Vui lòng chọn từ 1 đến 10 ghế.");
        }
        if (!"Tiền mặt".equals(request.hinhThucThanhToan())
                && !"Chuyển khoản".equals(request.hinhThucThanhToan())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        if (request.sanPham() != null && request.sanPham().size() > 20) {
            throw new IllegalArgumentException("Đơn hàng có quá nhiều loại sản phẩm.");
        }

    }

    private record BookingCustomer(String id, String name, String phone) {}
}
