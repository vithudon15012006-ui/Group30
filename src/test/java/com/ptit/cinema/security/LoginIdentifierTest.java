package com.ptit.cinema.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginIdentifierTest {
    @Test
    void normalizesPhoneFormattingForLogin() {
        assertEquals("0901234567", LoginIdentifier.normalizePhone("0901 234-567"));
    }

    @Test
    void normalizesEmailWithoutChangingPhoneCase() {
        assertEquals("staff@example.com", LoginIdentifier.normalize(" Staff@Example.com "));
    }

    @Test
    void rejectsPhoneWithLetters() {
        assertThrows(IllegalArgumentException.class, () -> LoginIdentifier.normalizePhone("090abc12345"));
    }

    @Test
    void rejectsPhoneOutsideSupportedLength() {
        assertThrows(IllegalArgumentException.class, () -> LoginIdentifier.normalizePhone("123"));
    }
}
