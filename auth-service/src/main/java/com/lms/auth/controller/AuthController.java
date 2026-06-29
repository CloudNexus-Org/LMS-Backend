package com.lms.auth.controller;

import com.lms.auth.dto.*;
import com.lms.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(authorization, request);
        return Map.of("message", "Logged out successfully");
    }

    @PostMapping("/refresh-token")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    public Map<String, String> resendOtp(@RequestBody ResendOtpRequest request) {
        return authService.resendOtp(request);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @GetMapping("/me")
    public UserInfoResponse me(@RequestHeader("Authorization") String authorization) {
        return authService.getCurrentUser(authorization);
    }

    @PostMapping("/validate-token")
    public TokenValidationResponse validateToken(@RequestBody ValidateTokenRequest request) {
        return authService.validateToken(request);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(authorization, request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "auth-service");
    }
}
