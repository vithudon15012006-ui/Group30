package com.ptit.cinema.security;

import java.util.Locale;

public final class LoginIdentifier {
    private LoginIdentifier() {}

    public static String normalize(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.contains("@")
                ? trimmed.toLowerCase(Locale.ROOT)
                : trimmed.replaceAll("[^0-9+]", "");
    }

    public static String normalizePhone(String value) {
        String input = value == null ? "" : value.trim();
        if (!input.matches("^[+0-9\\s().-]+$")) {
            throw new IllegalArgumentException("Số điện thoại chỉ được chứa chữ số và ký tự định dạng thông thường.");
        }
        String phone = normalize(value);
        if (!phone.matches("^\\+?[0-9]{7,15}$")) {
            throw new IllegalArgumentException("Số điện thoại phải có từ 7 đến 15 chữ số.");
        }
        return phone;
    }
}
