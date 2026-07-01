package com.lms.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReorderRequest {
    private List<ModuleOrder> modules;

    @Data
    public static class ModuleOrder {
        private Long moduleId;
        private Integer orderIndex;
        private List<LessonOrder> lessons;
    }

    @Data
    public static class LessonOrder {
        private Long lessonId;
        private Integer orderIndex;
    }
}
