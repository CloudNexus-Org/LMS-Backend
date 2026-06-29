package com.lms.catalog.dto;

import com.lms.catalog.model.Testimonial;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestimonialResponse {
    Long id;
    String name;
    String role;
    String company;
    String course;
    Integer rating;
    String avatar;
    String text;

    public static TestimonialResponse from(Testimonial t) {
        return TestimonialResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .role(t.getRole())
                .company(t.getCompany())
                .course(t.getCourse())
                .rating(t.getRating())
                .avatar(t.getAvatarUrl())
                .text(t.getQuote())
                .build();
    }
}
