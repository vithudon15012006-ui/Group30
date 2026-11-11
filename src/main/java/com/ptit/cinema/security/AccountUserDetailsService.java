package com.ptit.cinema.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;

    public AccountUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        String normalizedIdentifier = LoginIdentifier.normalize(identifier);
        var accounts = jdbc.query("""
                SELECT MaTaiKhoan, MatKhauHash, VaiTro, EmailDaXacMinh, BiKhoa
                FROM TaiKhoan
                WHERE (VaiTro = 'CUSTOMER' AND SoDienThoaiDangNhap = ?)
                   OR (VaiTro <> 'CUSTOMER' AND Email = ?)
                """, (rs, rowNum) -> new AccountAuthentication(
                rs.getString("MaTaiKhoan"),
                rs.getString("MatKhauHash"),
                rs.getString("VaiTro"),
                rs.getBoolean("EmailDaXacMinh"),
                rs.getBoolean("BiKhoa")
        ), normalizedIdentifier, normalizedIdentifier);
        if (accounts.isEmpty()) throw new UsernameNotFoundException("Invalid credentials");

        AccountAuthentication account = accounts.get(0);
        return User.withUsername(account.accountId())
                .password(account.passwordHash() == null ? "!" : account.passwordHash())
                .roles(account.role())
                .disabled("STAFF".equals(account.role()) && !account.emailVerified())
                .accountLocked(account.locked())
                .build();
    }

    private record AccountAuthentication(
            String accountId,
            String passwordHash,
            String role,
            boolean emailVerified,
            boolean locked
    ) {}
}
