package com.lms.catalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubmitCourseRequest {

    private String title;
    private String subtitle;
    private String category;
    private String level;
    private String description;
    private List<String> outcomes = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private Integer modules;
    private Integer lessons;
    private String pricingModel;
    private BigDecimal price;
    private String mentorName;
}
