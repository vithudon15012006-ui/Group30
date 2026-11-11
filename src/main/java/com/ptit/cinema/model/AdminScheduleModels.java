package com.ptit.cinema.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminScheduleModels {
    private AdminScheduleModels() {}

    public record Options(
            List<MovieOption> movies,
            List<RoomOption> rooms,
            List<ManagerOption> managers,
            TicketPrices prices
    ) {}

    public record MovieOption(String maPhim, String tenPhim) {}

    public record RoomOption(
            String maPhong,
            String tenPhong,
            String loaiPhong,
            Integer sucChua,
            int soGhe,
            long soSuatChieu
    ) {}

    public record ManagerOption(String maQuanLy, String hoTen) {}

    public record TicketPrices(int giaThuong, int giaVip, LocalDateTime capNhatLuc) {}

    public record PriceUpdate(int giaThuong, int giaVip) {}

    public record ScheduleRequest(
            String maPhim,
            String maPhong,
            String maQuanLy,
            LocalDate ngayChieu,
            String gioBatDau,
            String gioKetThuc
    ) {}

    public record ScheduleView(
            String maSuatChieu,
            String maPhim,
            String tenPhim,
            LocalDate ngayChieu,
            String gioBatDau,
            String gioKetThuc,
            String maPhong,
            String tenPhong,
            String maQuanLy,
            String tenQuanLy,
            Integer sucChua,
            int soGhe,
            int soVeDaBan,
            int soGheDangGiu,
            long soDonLienQuan
    ) {}
}
