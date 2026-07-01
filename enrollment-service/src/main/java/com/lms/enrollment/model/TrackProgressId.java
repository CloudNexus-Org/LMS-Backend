package com.lms.enrollment.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class TrackProgressId implements Serializable {
    private Long userId;
    private String trackId;
}
