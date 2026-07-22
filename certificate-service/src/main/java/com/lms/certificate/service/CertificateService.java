package com.lms.certificate.service;

import com.lms.certificate.client.UserClient;
import com.lms.certificate.dto.*;
import com.lms.certificate.event.CertificateEventProducer;
import com.lms.certificate.model.Certificate;
import com.lms.certificate.model.CertificateTemplate;
import com.lms.certificate.repository.CertificateRepository;
import com.lms.certificate.repository.CertificateTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateTemplateRepository templateRepository;
    private final CertificateEventProducer eventProducer;
    private final CertificateMetadataResolver metadataResolver;
    private final UserClient userClient;

    public List<CertificateResponse> myCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssueDateDesc(userId).stream()
                .map(CertificateResponse::from)
                .toList();
    }

    public CertificateResponse getCertificate(Long userId, String certificateId) {
        Certificate cert = certificateRepository.findByCodeAndUserId(certificateId, userId)
                .or(() -> parseNumericId(certificateId)
                        .flatMap(id -> certificateRepository.findByIdAndUserId(id, userId)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
        return CertificateResponse.from(cert);
    }

    private java.util.Optional<Long> parseNumericId(String value) {
        try {
            return java.util.Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ex) {
            return java.util.Optional.empty();
        }
    }

    public VerifyResponse verify(String code) {
        return certificateRepository.findByCode(code)
                .map(cert -> VerifyResponse.builder()
                        .valid(true)
                        .code(cert.getCode())
                        .recipient(cert.getRecipientName())
                        .title(cert.getTitle())
                        .track(cert.getTrack())
                        .issueDate(cert.getIssueDate() != null ? cert.getIssueDate().toString() : null)
                        .status(cert.getStatus())
                        .build())
                .orElse(VerifyResponse.builder().valid(false).code(code).build());
    }

    @Transactional
    public CertificateResponse generate(GenerateCertificateRequest request, String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !"INTERNAL".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin or internal access required");
        }
        if (request.getUserId() == null || request.getTrackId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId and trackId are required");
        }
        return CertificateResponse.from(
                generateFromTrackCompletion(request.getUserId(), request.getTrackId(), request.getRecipientName()));
    }

    @Transactional
    public CertificateResponse claimForStudent(Long userId, ClaimCertificateRequest request) {
        if (request.getTrackId() == null || request.getTrackId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trackId is required");
        }
        String recipient = userClient.displayName(userId).orElse("Student");
        return CertificateResponse.from(
                generateFromTrackCompletion(userId, request.getTrackId(), recipient, request.getCourseId()));
    }

    @Transactional
    public Certificate generateFromTrackCompletion(Long userId, String trackId, String recipientName) {
        return generateFromTrackCompletion(userId, trackId, recipientName, null);
    }

    @Transactional
    public Certificate generateFromTrackCompletion(Long userId, String trackId, String recipientName, Long courseIdHint) {
        if (certificateRepository.existsByUserIdAndTrackId(userId, trackId)) {
            return certificateRepository.findByUserIdOrderByIssueDateDesc(userId).stream()
                    .filter(c -> trackId.equals(c.getTrackId()))
                    .findFirst()
                    .orElseThrow();
        }
        TrackMetadata.Meta meta = metadataResolver.resolve(trackId, courseIdHint);
        String code = meta.codePrefix() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999);
        String verifyUrl = "realm.learn/verify/" + code;
        String recipient = recipientName != null && !recipientName.isBlank()
                ? recipientName
                : userClient.displayName(userId).orElse("Student");
        Certificate cert = certificateRepository.save(Certificate.builder()
                .code(code)
                .userId(userId)
                .trackId(trackId)
                .title(meta.title())
                .description(meta.description())
                .recipientName(recipient)
                .issueDate(LocalDate.now())
                .duration(meta.duration())
                .mentorName(meta.mentor())
                .track(meta.track())
                .status("verified")
                .verifyUrl(verifyUrl)
                .pdfUrl("/api/certificates/download/" + code + ".pdf")
                .build());
        eventProducer.publishCertificateIssued(userId, trackId, code);
        return cert;
    }

    public byte[] downloadPdf(Long userId, String certificateId) {
        Certificate cert = findOwnedCertificate(userId, certificateId);
        String content = "Certificate of Completion\n\n"
                + cert.getTitle() + "\n"
                + "Awarded to: " + cert.getRecipientName() + "\n"
                + "Code: " + cert.getCode();
        return content.getBytes();
    }

    public ShareResponse share(Long userId, String certificateId) {
        Certificate cert = findOwnedCertificate(userId, certificateId);
        String shareUrl = "https://" + cert.getVerifyUrl();
        String title = cert.getTitle();
        return ShareResponse.builder()
                .shareUrl(shareUrl)
                .linkedInUrl("https://www.linkedin.com/sharing/share-offsite/?url=" + shareUrl)
                .twitterUrl("https://twitter.com/intent/tweet?text=" + title + "&url=" + shareUrl)
                .build();
    }

    private Certificate findOwnedCertificate(Long userId, String certificateId) {
        return certificateRepository.findByCodeAndUserId(certificateId, userId)
                .or(() -> parseNumericId(certificateId)
                        .flatMap(id -> certificateRepository.findByIdAndUserId(id, userId)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found"));
    }
}
