package com.ptit.cinema.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ptit.cinema.service.SeatAlreadyBookedException;
import com.ptit.cinema.service.EmailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> duplicateRecord(DuplicateKeyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Dữ liệu đã tồn tại hoặc vừa được cập nhật bởi một thao tác khác."));
    }

    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<Map<String, String>> seatAlreadyBooked(SeatAlreadyBookedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<Map<String, String>> emailDelivery(EmailDeliveryException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<Map<String, String>> schemaNotReady(BadSqlGrammarException exception) {
        String databaseMessage = exception.getMostSpecificCause().getMessage();
        if (databaseMessage != null && databaseMessage.contains("TaiKhoan")) {
            log.warn("Account schema is missing; apply database/migrations/V1__account_security.sql");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message",
                            "Chưa tạo bảng tài khoản. Hãy import database/migrations/V1__account_security.sql vào database QuanLyRapChieuPhim rồi khởi động lại ứng dụng."));
        }
        if (databaseMessage != null && (databaseMessage.contains("CauHinhGiaVe")
                || databaseMessage.contains("ChiTietGheHoaDon")
                || databaseMessage.contains("GheGiuThanhToan"))) {
            log.warn("Booking schema is missing; apply database migrations V3 and V4");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message",
                            "Chưa cập nhật cấu trúc thanh toán/lịch chiếu. Hãy import V3__qr_transfer_payments.sql và V4__schedule_management.sql theo thứ tự vào QuanLyRapChieuPhim."));
        }
        log.error("Database query failed", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Truy vấn cơ sở dữ liệu thất bại. Kiểm tra log Spring Boot để biết chi tiết."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> databaseUnavailable(DataAccessException exception) {
        log.error("Database unavailable during API request", exception);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message",
                        "Không truy cập được cơ sở dữ liệu. Hãy bật MySQL, kiểm tra kết nối và xem log Spring Boot."));
    }
}
