package com.ptit.cinema.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AccountModels {
    private AccountModels() {}

    public record RegisterRequest(String hoTen, String sdt, String matKhau) {}

    public record LoginRequest(String identifier, String matKhau) {}

    public record SetPasswordRequest(String token, String matKhau) {}

    public record ChangePasswordRequest(String matKhauCu, String matKhauMoi) {}

    public record UpdateProfileRequest(String hoTen, String sdt) {}

    public record StaffInviteRequest(String maNhanVien, String email) {}

    public record AccountView(String vaiTro, String hoTen, String sdt) {}

    public record LockedAccount(
            String maTaiKhoan,
            String identifier,
            String vaiTro,
            String hoTen,
            LocalDateTime ngayTao
    ) {}

    public record OrderView(
            String maHoaDon,
            LocalDateTime thoiGianThanhToan,
            int thanhTien,
            String hinhThucThanhToan,
            String trangThaiThanhToan,
            String maThamChieuChuyenKhoan,
            LocalDateTime hetHanThanhToan,
            String qrImageUrl,
            List<TicketView> ve,
            List<ProductView> doAn
    ) {}

    public record TicketView(
            String maVe,
            int gia,
            String maGhe,
            String tenPhim,
            LocalDate ngayChieu,
            String gioBatDau,
            String tenPhong
    ) {}

    public record ProductView(String tenSanPham, int soLuong, int gia) {}
}
