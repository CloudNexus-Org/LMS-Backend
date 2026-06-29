package com.lms.mentor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mentor_students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MentorStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mentorUserId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long courseId;
    private String courseTitle;
    private Integer progress;
    private String status;
}
