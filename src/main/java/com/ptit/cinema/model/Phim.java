package com.ptit.cinema.model;

import java.time.LocalDate;

public record Phim(
        String maPhim,
        String tenPhim,
        Integer thoiLuong,
        LocalDate ngayKhoiChieu,
        Long doanhThuUsd
) {}
