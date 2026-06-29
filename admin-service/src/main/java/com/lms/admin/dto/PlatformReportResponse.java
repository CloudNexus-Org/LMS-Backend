package com.lms.admin.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class PlatformReportResponse {

    Map<String, Object> kpis;
    List<Map<String, Object>> topCourses;
    List<Map<String, Object>> topMentors;
    List<Map<String, Object>> categories;
    List<Map<String, Object>> geoData;
}
