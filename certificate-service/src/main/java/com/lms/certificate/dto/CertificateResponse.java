package com.lms.certificate.dto;

import com.lms.certificate.model.Certificate;
import lombok.Builder;
import lombok.Value;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Value
@Builder
public class CertificateResponse {
    String id;
    String title;
    String description;
    String issueDate;
    String duration;
    String recipient;
    String verifyLink;
    String track;
    String mentor;
    String status;

    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    public static CertificateResponse from(Certificate cert) {
        return CertificateResponse.builder()
                .id(cert.getCode())
                .title(cert.getTitle())
                .description(cert.getDescription())
                .issueDate(cert.getIssueDate() != null ? cert.getIssueDate().format(MONTH_FMT) : null)
                .duration(cert.getDuration())
                .recipient(cert.getRecipientName())
                .verifyLink(cert.getVerifyUrl())
                .track(cert.getTrack())
                .mentor(cert.getMentorName())
                .status(cert.getStatus())
                .build();
    }
}
