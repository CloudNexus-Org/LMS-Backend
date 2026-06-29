package com.lms.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "course_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseApproval {

    @Id
    @Column(name = "course_id")
    private String courseId;

    @Column(nullable = false)
    private String title;

    private String mentor;

    private String mentorAvatar;

    private String category;

    private String submitted;

    private Integer modules;

    private Integer lessons;

    private String duration;

    private Double previewRating;

    private String thumbnail;

    @Column(nullable = false)
    private String status;

    private String priority;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long mentorId;

    private Instant submittedAt;

    private Instant reviewedAt;

    private Long reviewedBy;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
