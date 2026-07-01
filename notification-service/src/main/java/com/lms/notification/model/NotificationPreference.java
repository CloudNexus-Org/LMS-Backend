package com.lms.notification.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email_enrollment", nullable = false)
    @Builder.Default
    private boolean emailEnrollment = true;

    @Column(name = "email_payment", nullable = false)
    @Builder.Default
    private boolean emailPayment = true;

    @Column(name = "email_assignment", nullable = false)
    @Builder.Default
    private boolean emailAssignment = true;

    @Column(name = "email_certificate", nullable = false)
    @Builder.Default
    private boolean emailCertificate = true;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private boolean pushEnabled = true;
}
