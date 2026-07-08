package com.lms.content.util;

import com.lms.content.model.CourseContent;

public final class CourseSubmissionRules {

    private CourseSubmissionRules() {
    }

    public static boolean isAutomatedTest(CourseContent course) {
        if (course == null) {
            return true;
        }
        String title = course.getTitle() != null ? course.getTitle().trim() : "";
        if (title.startsWith("Verification Test")) {
            return true;
        }
        String description = course.getDescription() != null ? course.getDescription() : "";
        return description.contains("automated API verification");
    }

    public static boolean isAutomatedTest(String title, String description) {
        if (title != null && title.trim().startsWith("Verification Test")) {
            return true;
        }
        return description != null && description.contains("automated API verification");
    }
}
