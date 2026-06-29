package com.lms.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformSetting {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    private Instant updatedAt;

    private Long updatedBy;
}
