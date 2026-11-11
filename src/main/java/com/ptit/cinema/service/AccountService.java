package com.ptit.cinema.service;

import com.ptit.cinema.model.AccountModels.AccountView;
import com.ptit.cinema.model.AccountModels.LockedAccount;
import com.ptit.cinema.model.AccountModels.OrderView;
import com.ptit.cinema.model.AccountModels.ProductView;
import com.ptit.cinema.model.AccountModels.TicketView;
import com.ptit.cinema.security.LoginIdentifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.ptit.cinema.model.AccountModels.*;

@Service
public class AccountService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final EmailTokenService emailTokens;
    private final VietQrService vietQr;

    public AccountService(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            EmailTokenService emailTokens,
            VietQrService vietQr
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.emailTokens = emailTokens;
        this.vietQr = vietQr;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String name = requiredText(request.hoTen(), "Vui lòng nhập họ tên.", 50);
        String phone = LoginIdentifier.normalizePhone(request.sdt());
        PasswordPolicy.validate(request.matKhau());

        if (jdbc.queryForObject("SELECT COUNT(*) FROM TaiKhoan WHERE SoDienThoaiDangNhap = ?", Integer.class, phone) > 0) {
            throw new IllegalArgumentException("Số điện thoại này đã có tài khoản. Hãy đăng nhập bằng số điện thoại đó.");
        }

        String customerId = "KH" + UUID.randomUUID().toString().replace("-", "");
        String accountId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO KhachHang (MaKhachHang, HoTen, Sdt, Email)
                VALUES (?, ?, ?, NULL)
                """, customerId, name, phone);
        jdbc.update("""
                INSERT INTO TaiKhoan
                    (MaTaiKhoan, Email, SoDienThoaiDangNhap, MatKhauHash, VaiTro, MaKhachHang, EmailDaXacMinh)
                VALUES (?, NULL, ?, ?, 'CUSTOMER', ?, TRUE)
                """, accountId, phone, passwordEncoder.encode(request.matKhau()), customerId);
    }

    public AccountView currentAccount(String accountId) {
        return jdbc.query("""
                SELECT A.VaiTro, K.HoTen, K.Sdt
                FROM TaiKhoan A
                LEFT JOIN KhachHang K ON K.MaKhachHang = A.MaKhachHang
                WHERE A.MaTaiKhoan = ?
                """, rs -> {
            if (!rs.next()) throw new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập.");
            return new AccountView(
                    rs.getString("VaiTro"),
                    rs.getString("HoTen"),
                    rs.getString("Sdt")
            );
        }, accountId);
    }

    @Transactional
    public AccountView updateProfile(String accountId, UpdateProfileRequest request) {
        String name = requiredText(request.hoTen(), "Vui lòng nhập họ tên.", 50);
        String phone = LoginIdentifier.normalizePhone(request.sdt());
        Integer phoneConflict = jdbc.queryForObject("""
                SELECT COUNT(*) FROM TaiKhoan
                WHERE SoDienThoaiDangNhap = ? AND MaTaiKhoan <> ?
                """, Integer.class, phone, accountId);
        if (phoneConflict != null && phoneConflict > 0) {
            throw new IllegalArgumentException("Số điện thoại này đã được liên kết với tài khoản khác.");
        }
        int updated = jdbc.update("""
                UPDATE KhachHang K
                JOIN TaiKhoan A ON A.MaKhachHang = K.MaKhachHang
                SET K.HoTen = ?, K.Sdt = ?, A.SoDienThoaiDangNhap = ?
                WHERE A.MaTaiKhoan = ? AND A.VaiTro = 'CUSTOMER'
                """, name, phone, phone, accountId);
        if (updated != 1) throw new IllegalStateException("Không thể cập nhật thông tin tài khoản.");
        return currentAccount(accountId);
    }

    @Transactional
    public void changePassword(String accountId, ChangePasswordRequest request) {
        if (request.matKhauCu() == null || request.matKhauCu().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
        }
        PasswordPolicy.validate(request.matKhauMoi());
        String currentHash = jdbc.query("""
                SELECT MatKhauHash FROM TaiKhoan WHERE MaTaiKhoan = ?
                """, rs -> rs.next() ? rs.getString(1) : null, accountId);
        if (currentHash == null || !passwordEncoder.matches(request.matKhauCu(), currentHash)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }
        if (passwordEncoder.matches(request.matKhauMoi(), currentHash)) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        jdbc.update("UPDATE TaiKhoan SET MatKhauHash = ? WHERE MaTaiKhoan = ?",
                passwordEncoder.encode(request.matKhauMoi()), accountId);
    }

    public List<OrderView> orders(String accountId) {
        String customerId = jdbc.query("""
                SELECT MaKhachHang FROM TaiKhoan
                WHERE MaTaiKhoan = ? AND VaiTro = 'CUSTOMER' AND BiKhoa = FALSE
                """, rs -> rs.next() ? rs.getString(1) : null, accountId);
        if (customerId == null) throw new IllegalStateException("Không tìm thấy tài khoản khách hàng.");
        List<OrderHeader> headers = jdbc.query("""
                SELECT MaHoaDon, ThoiGianThanhToan, ThanhTien, HinhThucThanhToan,
                       TrangThaiThanhToan, MaThamChieuChuyenKhoan, HetHanThanhToan
                FROM HoaDon
                WHERE MaKhachHang = ?
                ORDER BY ThoiGianThanhToan DESC, MaHoaDon DESC
                LIMIT 100
                """, (rs, rowNum) -> new OrderHeader(
                rs.getString("MaHoaDon"),
                rs.getTimestamp("ThoiGianThanhToan") == null ? null : rs.getTimestamp("ThoiGianThanhToan").toLocalDateTime(),
                rs.getInt("ThanhTien"),
                rs.getString("HinhThucThanhToan"),
                rs.getString("TrangThaiThanhToan"),
                rs.getString("MaThamChieuChuyenKhoan"),
                rs.getTimestamp("HetHanThanhToan") == null ? null : rs.getTimestamp("HetHanThanhToan").toLocalDateTime()
        ), customerId);

        List<OrderView> orders = new ArrayList<>();
        for (OrderHeader header : headers) {
            List<TicketView> tickets = jdbc.query("""
                    SELECT V.MaVe, C.Gia, C.MaGhe, P.TenPhim, SC.NgayChieu,
                           TIME_FORMAT(SC.GioBatDau, '%H:%i') AS GioBatDau, PH.TenPhong
                    FROM ChiTietGheHoaDon C
                    LEFT JOIN Ve V ON V.MaHoaDon = C.MaHoaDon AND V.MaGhe = C.MaGhe
                    JOIN SuatChieu SC ON SC.MaSuatChieu = C.MaSuatChieu
                    JOIN Phim P ON P.MaPhim = SC.MaPhim
                    JOIN Phong PH ON PH.MaPhong = C.MaPhong
                    WHERE C.MaHoaDon = ?
                    ORDER BY C.MaGhe
                    """, (rs, rowNum) -> new TicketView(
                    rs.getString("MaVe"),
                    rs.getInt("Gia"),
                    rs.getString("MaGhe"),
                    rs.getString("TenPhim"),
                    rs.getDate("NgayChieu").toLocalDate(),
                    rs.getString("GioBatDau"),
                    rs.getString("TenPhong")
            ), header.id());
            List<ProductView> products = jdbc.query("""
                    SELECT SP.TenSanPham, BG.SoLuong, SP.Gia
                    FROM BaoGom BG
                    JOIN SanPham SP ON SP.MaSanPham = BG.MaSanPham
                    WHERE BG.MaHoaDon = ?
                    ORDER BY SP.TenSanPham
                    """, (rs, rowNum) -> new ProductView(
                    rs.getString("TenSanPham"),
                    rs.getInt("SoLuong"),
                    rs.getInt("Gia")
            ), header.id());
            String qrImageUrl = "PENDING".equals(header.status()) && vietQr.isAvailable()
                    ? vietQr.imageUrl()
                    : null;
            orders.add(new OrderView(
                    header.id(), header.paidAt(), header.total(), header.paymentMethod(),
                    header.status(), header.reference(), header.expiresAt(), qrImageUrl, tickets, products
            ));
        }
        return orders;
    }

    public List<LockedAccount> lockedAccounts() {
        return jdbc.query("""
                SELECT A.MaTaiKhoan,
                       CASE WHEN A.VaiTro = 'CUSTOMER' THEN K.Sdt ELSE A.Email END AS LoginIdentifier,
                       A.VaiTro,
                       COALESCE(K.HoTen, NV.HoTen) AS HoTen, A.NgayTao
                FROM TaiKhoan A
                LEFT JOIN KhachHang K ON K.MaKhachHang = A.MaKhachHang
                LEFT JOIN NhanVien NV ON NV.MaNhanVien = A.MaNhanVien
                WHERE A.BiKhoa = TRUE
                ORDER BY A.NgayTao
                """, (rs, rowNum) -> new LockedAccount(
                rs.getString("MaTaiKhoan"),
                rs.getString("LoginIdentifier"),
                rs.getString("VaiTro"),
                rs.getString("HoTen"),
                rs.getTimestamp("NgayTao").toLocalDateTime()
        ));
    }

    @Transactional
    public void unlock(String accountId, String adminId) {
        Integer admin = jdbc.queryForObject(
                "SELECT COUNT(*) FROM TaiKhoan WHERE MaTaiKhoan = ? AND VaiTro = 'ADMIN'",
                Integer.class, adminId);
        if (admin == null || admin == 0) throw new IllegalStateException("Không xác định được admin đang thực hiện thao tác.");
        int updated = jdbc.update("""
                UPDATE TaiKhoan SET BiKhoa = FALSE, SoLanDangNhapSai = 0
                WHERE MaTaiKhoan = ? AND BiKhoa = TRUE
                """, accountId);
        if (updated == 0) throw new IllegalArgumentException("Tài khoản không tồn tại hoặc đã được mở khóa.");
        jdbc.update("""
                INSERT INTO NhatKyTaiKhoan (MaTaiKhoan, HanhDong, NguoiThucHien)
                VALUES (?, 'ADMIN_UNLOCK', ?)
                """, accountId, adminId);
    }

    @Transactional
    public void inviteStaff(StaffInviteRequest request) {
        String employeeId = requiredText(request.maNhanVien(), "Vui lòng chọn nhân viên.", 50);
        String email = normalizeEmail(request.email());
        Integer employeeExists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM NhanVien WHERE MaNhanVien = ?", Integer.class, employeeId);
        if (employeeExists == null || employeeExists == 0) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên đã chọn.");
        }
        var existing = jdbc.query("""
                SELECT MaTaiKhoan, VaiTro, MatKhauHash, EmailDaXacMinh
                FROM TaiKhoan WHERE MaNhanVien = ?
                """, (rs, rowNum) -> new PendingStaff(
                rs.getString("MaTaiKhoan"),
                rs.getString("VaiTro"),
                rs.getString("MatKhauHash"),
                rs.getBoolean("EmailDaXacMinh")
        ), employeeId);
        if (!existing.isEmpty()) {
            PendingStaff pending = existing.get(0);
            if (!"STAFF".equals(pending.role()) || pending.passwordHash() != null || pending.emailVerified()) {
                throw new IllegalArgumentException("Nhân viên này đã có một tài khoản đang hoạt động.");
            }
            Integer emailConflict = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM TaiKhoan WHERE Email = ? AND MaTaiKhoan <> ?",
                    Integer.class, email, pending.accountId());
            if (emailConflict != null && emailConflict > 0) {
                throw new IllegalArgumentException("Email đã được liên kết với một tài khoản khác.");
            }
            jdbc.update("UPDATE TaiKhoan SET Email = ? WHERE MaTaiKhoan = ?", email, pending.accountId());
            emailTokens.issue(pending.accountId(), email, "SET_PASSWORD", 48);
            return;
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM TaiKhoan WHERE Email = ?", Integer.class, email) > 0) {
            throw new IllegalArgumentException("Email đã được liên kết với một tài khoản khác.");
        }
        String accountId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO TaiKhoan
                    (MaTaiKhoan, Email, MatKhauHash, VaiTro, MaNhanVien, EmailDaXacMinh)
                VALUES (?, ?, NULL, 'STAFF', ?, FALSE)
                """, accountId, email, employeeId);
        emailTokens.issue(accountId, email, "SET_PASSWORD", 48);
    }

    public List<StaffOption> availableStaff() {
        return jdbc.query("""
                SELECT NV.MaNhanVien, NV.HoTen
                FROM NhanVien NV
                LEFT JOIN TaiKhoan A ON A.MaNhanVien = NV.MaNhanVien
                WHERE A.MaTaiKhoan IS NULL
                   OR (A.VaiTro = 'STAFF' AND A.MatKhauHash IS NULL AND A.EmailDaXacMinh = FALSE)
                ORDER BY NV.HoTen
                """, (rs, rowNum) -> new StaffOption(rs.getString("MaNhanVien"), rs.getString("HoTen")));
    }

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Vui lòng nhập email.");
        String email = value.trim().toLowerCase(Locale.ROOT);
        if (email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ.");
        }
        return email;
    }

    private static String requiredText(String value, String message, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private record OrderHeader(
            String id,
            LocalDateTime paidAt,
            int total,
            String paymentMethod,
            String status,
            String reference,
            LocalDateTime expiresAt
    ) {}

    public record StaffOption(String maNhanVien, String hoTen) {}

    private record PendingStaff(String accountId, String role, String passwordHash, boolean emailVerified) {}
}
