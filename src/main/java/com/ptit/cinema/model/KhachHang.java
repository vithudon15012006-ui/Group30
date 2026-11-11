package com.ptit.cinema.model;

import java.time.LocalDate;

public record KhachHang(
        String maKhachHang,
        String hoTen,
        LocalDate ngaySinh,
        String sdt,
        String email
) {}
