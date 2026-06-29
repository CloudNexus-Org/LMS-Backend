package com.lms.admin.dto;

import com.lms.admin.model.CourseApproval;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CourseApprovalResponse {

    String id;
    String title;
    String mentor;
    String mentorAvatar;
    String category;
    String submitted;
    Integer modules;
    Integer lessons;
    String duration;
    Double previewRating;
    String thumbnail;
    String status;
    String priority;
    String description;

    public static CourseApprovalResponse from(CourseApproval approval) {
        return CourseApprovalResponse.builder()
                .id(approval.getCourseId())
                .title(approval.getTitle())
                .mentor(approval.getMentor())
                .mentorAvatar(approval.getMentorAvatar())
                .category(approval.getCategory())
                .submitted(approval.getSubmitted())
                .modules(approval.getModules())
                .lessons(approval.getLessons())
                .duration(approval.getDuration())
                .previewRating(approval.getPreviewRating())
                .thumbnail(approval.getThumbnail())
                .status(approval.getStatus())
                .priority(approval.getPriority())
                .description(approval.getDescription())
                .build();
    }
}
