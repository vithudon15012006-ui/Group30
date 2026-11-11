package com.ptit.cinema.service;

import org.springframework.stereotype.Service;

@Service
public class VietQrService {
    private static final String IMAGE_URL = "/qr-chuyen-khoan-agribank.png";
    private static final String BANK_NAME = "Agribank";

    public boolean isAvailable() {
        return true;
    }

    public String imageUrl() {
        return IMAGE_URL;
    }

    public String bankName() {
        return BANK_NAME;
    }
}
