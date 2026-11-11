package com.ptit.cinema.security;

import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoginFailureListener {
    private static final int MAX_FAILURES = 5;

    private final JdbcTemplate jdbc;

    public LoginFailureListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener
    @Transactional
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String identifier = LoginIdentifier.normalize(event.getAuthentication().getName());
        var accounts = jdbc.query("""
                SELECT MaTaiKhoan, SoLanDangNhapSai, BiKhoa
                FROM TaiKhoan
                WHERE (VaiTro = 'CUSTOMER' AND SoDienThoaiDangNhap = ?)
                   OR (VaiTro <> 'CUSTOMER' AND Email = ?)
                FOR UPDATE
                """, (rs, rowNum) -> new AccountState(
                rs.getString("MaTaiKhoan"),
                rs.getInt("SoLanDangNhapSai"),
                rs.getBoolean("BiKhoa")
        ), identifier, identifier);
        if (accounts.isEmpty() || accounts.get(0).locked()) return;

        AccountState account = accounts.get(0);
        int failures = Math.min(MAX_FAILURES, account.failures() + 1);
        boolean locked = failures >= MAX_FAILURES;
        jdbc.update("""
                UPDATE TaiKhoan SET SoLanDangNhapSai = ?, BiKhoa = ?
                WHERE MaTaiKhoan = ?
                """, failures, locked, account.id());
        if (locked) {
            jdbc.update("""
                    INSERT INTO NhatKyTaiKhoan (MaTaiKhoan, HanhDong)
                    VALUES (?, 'AUTO_LOCK')
                    """, account.id());
        }
    }

    private record AccountState(String id, int failures, boolean locked) {}
}
