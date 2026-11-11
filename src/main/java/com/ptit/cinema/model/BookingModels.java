package com.ptit.cinema.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class BookingModels {
    private BookingModels() {}

    public record MovieOption(String maPhim, String tenPhim, Integer thoiLuong, LocalDate ngayKhoiChieu) {}

    public record ShowtimeOption(
            String maSuatChieu,
            String maPhim,
            String tenPhim,
            LocalDate ngayChieu,
            String gioBatDau,
            String gioKetThuc,
            String maPhong,
            String tenPhong,
            String loaiPhong,
            int sucChua,
            int soGheDaDat
    ) {}

    public record SeatOption(String maGhe, String hangGhe, String loaiGhe, boolean daDat, int gia) {}

    public record ProductOption(String maSanPham, String tenSanPham, int gia) {}

    public record BookingRequest(
            String maSuatChieu,
            List<String> ghe,
            List<ProductOrder> sanPham,
            String hinhThucThanhToan
    ) {}

    public record ProductOrder(String maSanPham, int soLuong) {}

    public record BookingReceipt(
            String maHoaDon,
            String tenPhim,
            LocalDate ngayChieu,
            String gioBatDau,
            String tenPhong,
            List<String> ghe,
            int tienVe,
            int tienDoAn,
            int thanhTien,
            String hinhThucThanhToan,
            String tenKhachHang,
            String sdt,
            String trangThaiThanhToan,
            String maThamChieuChuyenKhoan,
            LocalDateTime hetHanThanhToan,
            String qrImageUrl,
            String nganHang
    ) {}

    public record PaymentConfiguration(
            boolean chuyenKhoanDaCauHinh,
            String nganHang,
            String qrImageUrl,
            int thoiHanGiuGhePhut
    ) {}

    public record AdminPayment(
            String maHoaDon,
            String trangThaiThanhToan,
            String hinhThucThanhToan,
            int thanhTien,
            LocalDateTime thoiGianTao,
            LocalDateTime thoiGianThanhToan,
            LocalDateTime hetHanThanhToan,
            String maThamChieuChuyenKhoan,
            String tenKhachHang,
            String sdt,
            String xacNhanBoi,
            List<AdminPaymentSeat> ghe
    ) {}

    public record AdminPaymentSeat(
            String tenPhim,
            LocalDate ngayChieu,
            String gioBatDau,
            String tenPhong,
            String maGhe,
            int gia
    ) {}

    public record PaymentSummary(
            long tongDoanhThu,
            long doanhThuHomNay,
            long soGiaoDichDaThanhToan,
            long soDonChoThanhToan,
            long soDonHetHan
    ) {}
}
