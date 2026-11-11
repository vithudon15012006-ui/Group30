package com.ptit.cinema.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class EmailTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String mailFrom;

    public EmailTokenService(
            JdbcTemplate jdbc,
            JavaMailSender mailSender,
            @Value("${app.public-base-url}") String baseUrl,
            @Value("${app.mail-from:}") String mailFrom
    ) {
        this.jdbc = jdbc;
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.mailFrom = mailFrom;
    }

    public void issue(String accountId, String email, String purpose, int lifetimeHours) {
        jdbc.update("DELETE FROM TokenTaiKhoan WHERE MaTaiKhoan = ? AND MucDich = ?", accountId, purpose);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = sha256(rawToken);
        jdbc.update("""
                INSERT INTO TokenTaiKhoan (MaToken, MaTaiKhoan, MucDich, HetHanLuc)
                VALUES (?, ?, ?, ?)
                """, tokenHash, accountId, purpose, LocalDateTime.now().plusHours(lifetimeHours));

        String page = "SET_PASSWORD".equals(purpose) ? "/DatMatKhau.html" : "/XacMinhEmail.html";
        String link = UriComponentsBuilder.fromUriString(baseUrl).path(page)
                .queryParam("token", rawToken).build().encode().toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        if (!mailFrom.isBlank()) message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Xác nhận tài khoản PTIT Cinema");
        message.setText("""
                Xin chào,

                %s
                Liên kết có hiệu lực trong %d giờ:
                %s

                Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email.
                """.formatted(
                "SET_PASSWORD".equals(purpose)
                        ? "Hãy mở liên kết để đặt mật khẩu tài khoản nhân viên."
                        : "Hãy mở liên kết để xác minh email tài khoản.",
                lifetimeHours,
                link
        ));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new EmailDeliveryException("Không gửi được email. Hãy kiểm tra cấu hình SMTP rồi thử gửi lại.");
        }
    }

    @Transactional
    public void setInvitedPassword(String rawToken, String password, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        PasswordPolicy.validate(password);
        Token token = findValid(rawToken, "SET_PASSWORD");
        jdbc.update("""
                UPDATE TaiKhoan
                SET MatKhauHash = ?, EmailDaXacMinh = TRUE
                WHERE MaTaiKhoan = ? AND VaiTro = 'STAFF'
                """, encoder.encode(password), token.accountId());
        jdbc.update("DELETE FROM TokenTaiKhoan WHERE MaTaiKhoan = ?", token.accountId());
    }

    private Token findValid(String rawToken, String purpose) {
        if (rawToken == null || rawToken.length() < 40 || rawToken.length() > 100) {
            throw new IllegalArgumentException("Liên kết không hợp lệ hoặc đã hết hạn.");
        }
        var tokens = jdbc.query("""
                SELECT MaTaiKhoan, HetHanLuc
                FROM TokenTaiKhoan
                WHERE MaToken = ? AND MucDich = ? AND HetHanLuc > ?
                """, (rs, rowNum) -> new Token(
                rs.getString("MaTaiKhoan"),
                rs.getTimestamp("HetHanLuc").toLocalDateTime()
        ), sha256(rawToken), purpose, LocalDateTime.now());
        if (tokens.isEmpty()) throw new IllegalArgumentException("Liên kết không hợp lệ hoặc đã hết hạn.");
        return tokens.get(0);
    }

    private static String sha256(String token) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng.", exception);
        }
    }

    private record Token(String accountId, LocalDateTime expiresAt) {}
}
