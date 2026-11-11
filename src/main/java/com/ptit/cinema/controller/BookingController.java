package com.ptit.cinema.controller;

import com.ptit.cinema.model.BookingModels.BookingReceipt;
import com.ptit.cinema.model.BookingModels.BookingRequest;
import com.ptit.cinema.model.BookingModels.MovieOption;
import com.ptit.cinema.model.BookingModels.ProductOption;
import com.ptit.cinema.model.BookingModels.PaymentConfiguration;
import com.ptit.cinema.model.BookingModels.SeatOption;
import com.ptit.cinema.model.BookingModels.ShowtimeOption;
import com.ptit.cinema.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dat-ve")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/phim")
    public List<MovieOption> movies() {
        return bookingService.movies();
    }

    @GetMapping("/suat-chieu")
    public List<ShowtimeOption> showtimes(@RequestParam(required = false) String maPhim) {
        return bookingService.showtimes(maPhim);
    }

    @GetMapping("/suat-chieu/{id}/ghe")
    public List<SeatOption> seats(@PathVariable String id) {
        return bookingService.seats(id);
    }

    @GetMapping("/do-an")
    public List<ProductOption> products() {
        return bookingService.products();
    }

    @GetMapping("/payment-config")
    public PaymentConfiguration paymentConfiguration() {
        return bookingService.paymentConfiguration();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingReceipt create(@RequestBody BookingRequest request, Authentication authentication) {
        return bookingService.createBooking(request, authentication.getName());
    }
}
