package com.lms.certificate.controller;

import com.lms.certificate.dto.*;
import com.lms.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/me")
    public List<CertificateResponse> myCertificates(@RequestHeader("X-User-Id") Long userId) {
        return certificateService.myCertificates(userId);
    }

    @GetMapping("/verify/{certificateCode}")
    public VerifyResponse verify(@PathVariable String certificateCode) {
        return certificateService.verify(certificateCode);
    }

    @GetMapping("/{certificateId}")
    public CertificateResponse getCertificate(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String certificateId) {
        return certificateService.getCertificate(userId, certificateId);
    }

    @PostMapping("/generate")
    public CertificateResponse generate(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody GenerateCertificateRequest request) {
        return certificateService.generate(request, role != null ? role : "INTERNAL");
    }

    @GetMapping("/{certificateId}/download")
    public ResponseEntity<byte[]> download(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String certificateId) {
        byte[] pdf = certificateService.downloadPdf(userId, certificateId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{certificateId}/share")
    public ShareResponse share(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String certificateId) {
        return certificateService.share(userId, certificateId);
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP", "service", "certificate-service");
    }
}
