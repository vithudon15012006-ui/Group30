package com.ptit.cinema.controller;

import com.ptit.cinema.model.AccountModels.LoginRequest;
import com.ptit.cinema.model.AccountModels.RegisterRequest;
import com.ptit.cinema.model.AccountModels.SetPasswordRequest;
import com.ptit.cinema.service.AccountService;
import com.ptit.cinema.service.EmailTokenService;
import com.ptit.cinema.security.LoginIdentifier;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.ptit.cinema.model.AccountModels.AccountView;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AccountService accounts;
    private final EmailTokenService emailTokens;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    public AuthController(
            AuthenticationManager authenticationManager,
            AccountService accounts,
            EmailTokenService emailTokens,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbc
    ) {
        this.authenticationManager = authenticationManager;
        this.accounts = accounts;
        this.emailTokens = emailTokens;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Vui lòng đăng nhập để tiếp tục."));
        }
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .findFirst().orElse("");
        return ResponseEntity.ok(Map.of("maTaiKhoan", authentication.getName(), "vaiTro", role));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        accounts.register(request);
        return ResponseEntity.accepted().body(Map.of(
                "message", "Tạo tài khoản thành công. Bạn có thể đăng nhập bằng số điện thoại và mật khẩu."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        if (request.identifier() == null || request.identifier().isBlank()
                || request.matKhau() == null || request.matKhau().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập số điện thoại/email và mật khẩu."));
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.identifier().trim(), request.matKhau()));
            servletRequest.getSession(true);
            servletRequest.changeSessionId();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, servletRequest, servletResponse);
            jdbc.update("UPDATE TaiKhoan SET SoLanDangNhapSai = 0 WHERE MaTaiKhoan = ?",
                    authentication.getName());
            AccountView account = accounts.currentAccount(authentication.getName());
            return ResponseEntity.ok(account);
        } catch (BadCredentialsException exception) {
            if (isLocked(request.identifier())) {
                return ResponseEntity.status(423).body(Map.of(
                        "message", "Tài khoản đã bị khóa sau 5 lần đăng nhập sai. Vui lòng liên hệ admin để mở khóa."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Số điện thoại/email hoặc mật khẩu không chính xác."));
        } catch (LockedException exception) {
            return ResponseEntity.status(423).body(Map.of(
                    "message", "Tài khoản đã bị khóa. Vui lòng liên hệ admin để mở khóa."));
        } catch (DisabledException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", "Tài khoản nhân viên chưa được kích hoạt. Vui lòng liên hệ quản trị viên."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/set-staff-password")
    public Map<String, String> setStaffPassword(@RequestBody SetPasswordRequest request) {
        emailTokens.setInvitedPassword(request.token(), request.matKhau(), passwordEncoder);
        return Map.of("message", "Đã kích hoạt tài khoản nhân viên. Bạn có thể đăng nhập.");
    }

    private boolean isLocked(String identifier) {
        String normalizedIdentifier = LoginIdentifier.normalize(identifier);
        Integer locked = jdbc.query("""
                SELECT BiKhoa FROM TaiKhoan
                WHERE (VaiTro = 'CUSTOMER' AND SoDienThoaiDangNhap = ?)
                   OR (VaiTro <> 'CUSTOMER' AND Email = ?)
                """, rs -> rs.next() ? rs.getInt(1) : 0, normalizedIdentifier, normalizedIdentifier);
        return locked != null && locked == 1;
    }
}
