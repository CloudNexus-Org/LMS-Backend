package com.lms.auth.service;

import com.lms.auth.dto.*;
import com.lms.auth.event.AuthEventProducer;
import com.lms.auth.model.*;
import com.lms.auth.repository.*;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String OTP_PURPOSE_PASSWORD_RESET = "PASSWORD_RESET";

    private final AuthCredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JwtService jwtService;
    private final AuthEventProducer eventProducer;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-expiry}")
    private Duration refreshExpiry;

    @Value("${jwt.remember-me-refresh-expiry}")
    private Duration rememberMeRefreshExpiry;

    @Value("${auth.otp.length:6}")
    private int otpLength;

    @Value("${auth.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${auth.login.max-attempts:5}")
    private int maxLoginAttempts;

    @Value("${auth.login.lock-minutes:15}")
    private int loginLockMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (credentialRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        AuthCredential user = credentialRepository.save(AuthCredential.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(UserRole.STUDENT)
                .active(true)
                .build());

        eventProducer.publishUserRegistered(
                user.getId(), user.getEmail(), user.getRole().name(), user.getFullName());

        return buildAuthResponse(user, false);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ip) {
        String email = normalizeEmail(request.getEmail());
        assertAccountNotLocked(email);

        AuthCredential user = credentialRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> {
                    recordLoginAttempt(email, ip, false);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
                });

        if (!user.isActive()) {
            recordLoginAttempt(email, ip, false);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginAttempt(email, ip, false);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        recordLoginAttempt(email, ip, true);
        return buildAuthResponse(user, request.isRememberMe());
    }

    @Transactional
    public void logout(String accessToken, RefreshTokenRequest refreshRequest) {
        Long userId = resolveUserIdFromBearer(accessToken);

        if (refreshRequest != null && refreshRequest.getRefreshToken() != null
                && !refreshRequest.getRefreshToken().isBlank()) {
            revokeRefreshToken(refreshRequest.getRefreshToken());
        } else {
            var tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
            tokens.forEach(token -> token.setRevoked(true));
            refreshTokenRepository.saveAll(tokens);
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        AuthCredential user = credentialRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        boolean rememberMe = stored.getExpiresAt().isAfter(Instant.now().plus(refreshExpiry));
        return buildAuthResponse(user, rememberMe);
    }

    @Transactional
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        credentialRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        generateAndSendOtp(email, OTP_PURPOSE_PASSWORD_RESET);
        return Map.of("message", "OTP sent to your email");
    }

    @Transactional
    public Map<String, String> verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String purpose = normalizePurpose(request.getPurpose());
        OtpCode otp = findValidOtp(email, request.getCode(), purpose);

        otp.setVerified(true);
        otpCodeRepository.save(otp);
        return Map.of("message", "OTP verified successfully");
    }

    @Transactional
    public Map<String, String> resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String purpose = normalizePurpose(request.getPurpose());

        credentialRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        generateAndSendOtp(email, purpose);
        return Map.of("message", "OTP resent successfully");
    }

    @Transactional
    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        String purpose = normalizePurpose(request.getPurpose());

        AuthCredential user = credentialRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        OtpCode otp = otpCodeRepository
                .findTopByEmailIgnoreCaseAndCodeAndPurposeAndUsedFalseOrderByExpiresAtDesc(
                        email, request.getCode(), purpose)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP"));

        if (!otp.isVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not verified");
        }
        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credentialRepository.save(user);

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        var tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);

        eventProducer.publishPasswordReset(user.getId(), user.getEmail());
        return Map.of("message", "Password reset successfully");
    }

    public UserInfoResponse getCurrentUser(String accessToken) {
        Long userId = resolveUserIdFromBearer(accessToken);
        AuthCredential user = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUserInfo(user);
    }

    public TokenValidationResponse validateToken(ValidateTokenRequest request) {
        try {
            Claims claims = jwtService.parseToken(request.getToken());
            if (!jwtService.isAccessToken(claims) || claims.getExpiration().before(new java.util.Date())) {
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid or expired token")
                        .build();
            }

            return TokenValidationResponse.builder()
                    .valid(true)
                    .userId(jwtService.getUserId(claims))
                    .email(jwtService.getEmail(claims))
                    .role(jwtService.getRole(claims).name().toLowerCase())
                    .build();
        } catch (Exception ex) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token")
                    .build();
        }
    }

    @Transactional
    public Map<String, String> changePassword(String accessToken, ChangePasswordRequest request) {
        Long userId = resolveUserIdFromBearer(accessToken);
        AuthCredential user = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        credentialRepository.save(user);

        var tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);

        eventProducer.publishPasswordReset(user.getId(), user.getEmail());
        return Map.of("message", "Password changed successfully");
    }

    private AuthResponse buildAuthResponse(AuthCredential user, boolean rememberMe) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user.getId(), rememberMe);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserInfo(user))
                .build();
    }

    private String createRefreshToken(Long userId, boolean rememberMe) {
        String rawToken = UUID.randomUUID() + "." + UUID.randomUUID();
        Duration expiry = rememberMe ? rememberMeRefreshExpiry : refreshExpiry;
        Instant expiresAt = Instant.now().plus(expiry);

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(userId)
                .tokenHash(hashToken(rawToken))
                .expiresAt(expiresAt)
                .revoked(false)
                .build());

        return rawToken;
    }

    private void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private void generateAndSendOtp(String email, String purpose) {
        String code = generateOtpCode();
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(otpExpiryMinutes));

        otpCodeRepository.save(OtpCode.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .used(false)
                .verified(false)
                .build());

        eventProducer.publishOtpSent(email, purpose, expiresAt);
    }

    private OtpCode findValidOtp(String email, String code, String purpose) {
        OtpCode otp = otpCodeRepository
                .findTopByEmailIgnoreCaseAndCodeAndPurposeAndUsedFalseOrderByExpiresAtDesc(email, code, purpose)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP"));

        if (otp.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }
        return otp;
    }

    private void assertAccountNotLocked(String email) {
        Instant since = Instant.now().minus(Duration.ofMinutes(loginLockMinutes));
        long failedAttempts = loginAttemptRepository.countByEmailIgnoreCaseAndSuccessFalseAndAttemptedAtAfter(
                email, since);
        if (failedAttempts >= maxLoginAttempts) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Account locked due to too many failed login attempts. Try again later.");
        }
    }

    private void recordLoginAttempt(String email, String ip, boolean success) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .email(email)
                .ip(ip)
                .success(success)
                .attemptedAt(Instant.now())
                .build());
    }

    private Long resolveUserIdFromBearer(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        try {
            Claims claims = jwtService.parseToken(token);
            if (!jwtService.isAccessToken(claims)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
            }
            return jwtService.getUserId(claims);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        return authorizationHeader.substring(7).trim();
    }

    private UserInfoResponse toUserInfo(AuthCredential user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name().toLowerCase())
                .username(deriveUsername(user.getEmail()))
                .build();
    }

    private String deriveUsername(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, otpLength);
        int code = secureRandom.nextInt(bound);
        return String.format("%0" + otpLength + "d", code);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizePurpose(String purpose) {
        return purpose == null || purpose.isBlank() ? OTP_PURPOSE_PASSWORD_RESET : purpose.trim().toUpperCase();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
