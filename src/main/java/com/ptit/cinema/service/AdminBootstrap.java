package com.ptit.cinema.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin-email:}") String email,
            @Value("${app.bootstrap-admin-password:}") String password
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configurez ensemble APP_BOOTSTRAP_ADMIN_EMAIL et APP_BOOTSTRAP_ADMIN_PASSWORD.");
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || normalizedEmail.length() > 254) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_EMAIL n'est pas valide.");
        }
        if (password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Le mot de passe admin doit faire au moins 12 caractères et au plus 72 octets UTF-8.");
        }

        var existing = jdbc.query("""
                SELECT VaiTro FROM TaiKhoan WHERE Email = ?
                """, (rs, rowNum) -> rs.getString(1), normalizedEmail);
        if (!existing.isEmpty()) {
            if (!"ADMIN".equals(existing.get(0))) {
                throw new IllegalStateException("L'email bootstrap est déjà lié à un compte non administrateur.");
            }
            return;
        }
        jdbc.update("""
                INSERT INTO TaiKhoan
                    (MaTaiKhoan, Email, MatKhauHash, VaiTro, EmailDaXacMinh)
                VALUES (?, ?, ?, 'ADMIN', TRUE)
                """, UUID.randomUUID().toString(), normalizedEmail, passwordEncoder.encode(password));
    }
}
