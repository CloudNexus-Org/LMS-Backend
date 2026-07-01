package com.lms.analytics.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_activity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentActivity {
    @EmbeddedId
    private StudentActivityId id;

    @Builder.Default
    private Integer lessonsCompleted = 0;

    @Builder.Default
    private Integer minutesLearned = 0;

    @Builder.Default
    private Integer quizzesTaken = 0;
}
