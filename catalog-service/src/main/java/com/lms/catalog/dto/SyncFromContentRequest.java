package com.lms.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SyncFromContentRequest {

    private Long contentId;
    private Long catalogCourseId;
    private Long mentorId;
    private String title;
    private String description;
    private String category;
    private String level;
    private Integer modules;
    private Integer lessons;
    private BigDecimal price;
    private String thumbnailUrl;
    private String mentorName;
    private List<String> outcomes = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
}
