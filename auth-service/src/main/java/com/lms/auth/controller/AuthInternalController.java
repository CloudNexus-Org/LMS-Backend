package com.lms.auth.controller;

import com.lms.auth.dto.ProvisionCredentialRequest;
import com.lms.auth.dto.SyncProfileNameRequest;
import com.lms.auth.model.UserRole;
import com.lms.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class AuthInternalController {

    private final AuthService authService;

    @PostMapping("/provision")
    public void provision(@RequestBody ProvisionCredentialRequest request) {
        UserRole role = UserRole.valueOf(
                request.getRole() != null ? request.getRole().toUpperCase() : "MENTOR");
        authService.provisionCredential(
                request.getUserId(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                role);
    }

    @PutMapping("/profile-name")
    public void syncProfileName(@RequestBody SyncProfileNameRequest request) {
        authService.syncProfileName(request.getEmail(), request.getFullName());
    }
}
