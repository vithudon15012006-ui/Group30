package com.ptit.cinema.controller;

import com.ptit.cinema.model.AccountModels.LockedAccount;
import com.ptit.cinema.model.AccountModels.StaffInviteRequest;
import com.ptit.cinema.service.AccountService;
import com.ptit.cinema.service.EmailDeliveryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {
    private final AccountService accounts;

    public AdminAccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/locked")
    public List<LockedAccount> lockedAccounts() {
        return accounts.lockedAccounts();
    }

    @PostMapping("/{id}/unlock")
    public Map<String, String> unlock(@PathVariable String id, Authentication authentication) {
        accounts.unlock(id, authentication.getName());
        return Map.of("message", "Đã mở khóa tài khoản. Người dùng có thể đăng nhập lại.");
    }

    @GetMapping("/available-staff")
    public List<AccountService.StaffOption> availableStaff() {
        return accounts.availableStaff();
    }

    @PostMapping("/staff-invitations")
    public ResponseEntity<Map<String, String>> inviteStaff(@RequestBody StaffInviteRequest request) {
        try {
            accounts.inviteStaff(request);
            return ResponseEntity.accepted().body(Map.of(
                    "message", "Đã gửi thư mời. Nhân viên cần mở email để đặt mật khẩu và xác minh địa chỉ."));
        } catch (EmailDeliveryException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", exception.getMessage()));
        }
    }
}
