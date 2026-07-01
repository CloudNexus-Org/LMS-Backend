package com.lms.certificate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String trackId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String recipientName;

    private LocalDate issueDate;

    private String duration;

    private String mentorName;

    private String track;

    @Builder.Default
    private String status = "verified";

    private String verifyUrl;

    private String pdfUrl;

    @CreationTimestamp
    private Instant createdAt;
}
