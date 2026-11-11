package com.ptit.cinema.controller;

import com.ptit.cinema.model.AccountModels.AccountView;
import com.ptit.cinema.model.AccountModels.ChangePasswordRequest;
import com.ptit.cinema.model.AccountModels.OrderView;
import com.ptit.cinema.model.AccountModels.UpdateProfileRequest;
import com.ptit.cinema.service.AccountService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public AccountView current(Authentication authentication) {
        return accounts.currentAccount(authentication.getName());
    }

    @PutMapping
    public AccountView update(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        return accounts.updateProfile(authentication.getName(), request);
    }

    @PutMapping("/password")
    public Map<String, String> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request
    ) {
        accounts.changePassword(authentication.getName(), request);
        return Map.of("message", "Đã cập nhật mật khẩu.");
    }

    @GetMapping("/orders")
    public List<OrderView> orders(Authentication authentication) {
        return accounts.orders(authentication.getName());
    }
}
