package com.ptit.cinema.service;

public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}
