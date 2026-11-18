package com.ptit.cinema.service;

import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static void validateRegistration(String password) {
        if (password == null || password.length() < 8 || password.length() > 16
                || !password.matches("(?s).*[A-Z].*")
                || !password.matches("(?s).*[a-z].*")
                || !password.matches("(?s).*[0-9].*")
                || !password.matches("(?s).*[^A-Za-z0-9\\s].*")) {
            throw new IllegalArgumentException("Mật khẩu cần 8 đến 16 ký tự, ít nhất 1 chữ in hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt.");
        }
    }

    public static void validate(String password) {
        if (password == null || password.length() < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu cần ít nhất 12 ký tự và tối đa 72 byte UTF-8.");
        }
    }
}
