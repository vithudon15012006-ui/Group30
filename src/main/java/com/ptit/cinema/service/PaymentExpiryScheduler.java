package com.ptit.cinema.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentExpiryScheduler {
    private final BookingService bookings;

    public PaymentExpiryScheduler(BookingService bookings) {
        this.bookings = bookings;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expirePendingPayments() {
        bookings.expirePendingTransfers();
    }
}
