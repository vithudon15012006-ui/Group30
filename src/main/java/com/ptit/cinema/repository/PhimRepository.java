package com.ptit.cinema.repository;

import com.ptit.cinema.model.Phim;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PhimRepository {
    private final JdbcTemplate jdbc;

    public PhimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Phim> findAll() {
        return jdbc.query("""
                SELECT MaPhim, TenPhim, ThoiLuong, NgayKhoiChieu, DoanhThuUSD
                FROM Phim ORDER BY MaPhim
                """, (rs, rowNum) -> new Phim(
                rs.getString("MaPhim"),
                rs.getString("TenPhim"),
                (Integer) rs.getObject("ThoiLuong"),
                rs.getDate("NgayKhoiChieu") == null ? null : rs.getDate("NgayKhoiChieu").toLocalDate(),
                (Long) rs.getObject("DoanhThuUSD")
        ));
    }

    public List<Phim> search(String keyword) {
        String q = "%" + keyword.trim() + "%";
        return jdbc.query("""
                SELECT MaPhim, TenPhim, ThoiLuong, NgayKhoiChieu, DoanhThuUSD
                FROM Phim
                WHERE MaPhim LIKE ? OR TenPhim LIKE ?
                ORDER BY MaPhim
                """, (rs, rowNum) -> new Phim(
                rs.getString("MaPhim"),
                rs.getString("TenPhim"),
                (Integer) rs.getObject("ThoiLuong"),
                rs.getDate("NgayKhoiChieu") == null ? null : rs.getDate("NgayKhoiChieu").toLocalDate(),
                (Long) rs.getObject("DoanhThuUSD")
        ), q, q);
    }

    public int insert(Phim p) {
        return jdbc.update("""
                INSERT INTO Phim(MaPhim, TenPhim, ThoiLuong, NgayKhoiChieu, DoanhThuUSD)
                VALUES (?, ?, ?, ?, ?)
                """, p.maPhim(), p.tenPhim(), p.thoiLuong(), p.ngayKhoiChieu(), p.doanhThuUsd());
    }

    public int update(String id, Phim p) {
        return jdbc.update("""
                UPDATE Phim
                SET TenPhim = ?, ThoiLuong = ?, NgayKhoiChieu = ?, DoanhThuUSD = ?
                WHERE MaPhim = ?
                """, p.tenPhim(), p.thoiLuong(), p.ngayKhoiChieu(), p.doanhThuUsd(), id);
    }

    public int delete(String id) {
        return jdbc.update("DELETE FROM Phim WHERE MaPhim = ?", id);
    }
}
