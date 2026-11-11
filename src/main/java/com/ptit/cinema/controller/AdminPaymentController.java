package com.ptit.cinema.controller;

import com.ptit.cinema.model.BookingModels.AdminPayment;
import com.ptit.cinema.model.BookingModels.PaymentSummary;
import com.ptit.cinema.service.AdminPaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {
    private final AdminPaymentService payments;

    public AdminPaymentController(AdminPaymentService payments) {
        this.payments = payments;
    }

    @GetMapping("/summary")
    public PaymentSummary summary() {
        return payments.summary();
    }

    @GetMapping
    public List<AdminPayment> payments(@RequestParam(defaultValue = "ALL") String status) {
        return payments.payments(status);
    }

    @PostMapping("/{invoiceId}/confirm")
    public Map<String, String> confirm(
            @PathVariable String invoiceId,
            Authentication authentication
    ) {
        payments.confirmTransfer(invoiceId, authentication.getName());
        return Map.of("message", "Đã xác nhận tiền vào tài khoản và hoàn tất đơn hàng.");
    }

    @PostMapping("/{invoiceId}/cancel")
    public Map<String, String> cancel(
            @PathVariable String invoiceId,
            Authentication authentication
    ) {
        payments.cancelTransfer(invoiceId, authentication.getName());
        return Map.of("message", "Đã hủy đơn chờ thanh toán và giải phóng ghế.");
    }
}
