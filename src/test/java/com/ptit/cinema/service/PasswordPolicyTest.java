package com.ptit.cinema.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {
    @Test
    void acceptsLongPasswordWithinBcryptByteLimit() {
        assertDoesNotThrow(() -> PasswordPolicy.validate("correct horse battery"));
    }

    @Test
    void rejectsPasswordsShorterThanTwelveCharacters() {
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.validate("short-pass"));
    }

    @Test
    void rejectsPasswordsBeyondBcryptUtf8Limit() {
        assertThrows(IllegalArgumentException.class, () -> PasswordPolicy.validate("ắ".repeat(25)));
    }
}
