package com.ptit.cinema.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VietQrServiceTest {
    @Test
    void providedStaticQrImageIsReadableAndDoesNotContainAnOrderAmount() throws Exception {
        VietQrService service = new VietQrService();
        assertTrue(service.isAvailable());
        assertEquals("/qr-chuyen-khoan-agribank.png", service.imageUrl());

        try (InputStream stream = getClass().getResourceAsStream("/static/qr-chuyen-khoan-agribank.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            String payload = new MultiFormatReader().decode(
                    new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))),
                    hints
            ).getText();

            assertTrue(payload.startsWith("000201"));
            assertTrue(payload.contains("QRIBFTTA"));
            assertTrue(topLevelTags(payload).contains("38"));
            assertFalse(topLevelTags(payload).contains("54"));
        }
    }

    private static Set<String> topLevelTags(String payload) {
        Set<String> tags = new HashSet<>();
        int index = 0;
        while (index + 4 <= payload.length()) {
            String tag = payload.substring(index, index + 2);
            int length = Integer.parseInt(payload.substring(index + 2, index + 4));
            int valueEnd = index + 4 + length;
            if (valueEnd > payload.length()) throw new IllegalArgumentException("Invalid QR payload.");
            tags.add(tag);
            index = valueEnd;
            if ("63".equals(tag)) break;
        }
        return tags;
    }
}
