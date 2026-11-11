package com.ptit.cinema.repository;

import com.ptit.cinema.model.KhachHang;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KhachHangRepository {
    private final JdbcTemplate jdbc;

    public KhachHangRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<KhachHang> findAll() {
        return jdbc.query("""
                SELECT MaKhachHang, HoTen, NgaySinh, Sdt, Email
                FROM KhachHang ORDER BY MaKhachHang
                """, (rs, rowNum) -> new KhachHang(
                rs.getString("MaKhachHang"),
                rs.getString("HoTen"),
                rs.getDate("NgaySinh") == null ? null : rs.getDate("NgaySinh").toLocalDate(),
                rs.getString("Sdt"),
                rs.getString("Email")
        ));
    }

    public List<KhachHang> search(String keyword) {
        String q = "%" + keyword.trim() + "%";
        return jdbc.query("""
                SELECT MaKhachHang, HoTen, NgaySinh, Sdt, Email
                FROM KhachHang
                WHERE MaKhachHang LIKE ? OR HoTen LIKE ? OR Sdt LIKE ? OR Email LIKE ?
                ORDER BY MaKhachHang
                """, (rs, rowNum) -> new KhachHang(
                rs.getString("MaKhachHang"),
                rs.getString("HoTen"),
                rs.getDate("NgaySinh") == null ? null : rs.getDate("NgaySinh").toLocalDate(),
                rs.getString("Sdt"),
                rs.getString("Email")
        ), q, q, q, q);
    }

    public int insert(KhachHang k) {
        return jdbc.update("""
                INSERT INTO KhachHang(MaKhachHang, HoTen, NgaySinh, Sdt, Email)
                VALUES (?, ?, ?, ?, ?)
                """, k.maKhachHang(), k.hoTen(), k.ngaySinh(), k.sdt(), k.email());
    }

    public int update(String id, KhachHang k) {
        return jdbc.update("""
                UPDATE KhachHang
                SET HoTen = ?, NgaySinh = ?, Sdt = ?, Email = ?
                WHERE MaKhachHang = ?
                """, k.hoTen(), k.ngaySinh(), k.sdt(), k.email(), id);
    }

    public int delete(String id) {
        return jdbc.update("DELETE FROM KhachHang WHERE MaKhachHang = ?", id);
    }
}
