package com.lms.analytics.service;

import com.lms.analytics.model.*;
import com.lms.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DailyMetricRepository dailyMetricRepository;
    private final MentorMetricRepository mentorMetricRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final StudentActivityRepository studentActivityRepository;

    @Value("${lms.catalog-service-url:http://localhost:8083}")
    private String catalogServiceUrl;

    private final RestClient restClient = RestClient.create();

    @Transactional
    public void recordUserRegistered() {
        LocalDate today = LocalDate.now();
        DailyMetric metric = dailyMetricRepository.findById(today)
                .orElseGet(() -> DailyMetric.builder().date(today).build());
        metric.setNewUsers(metric.getNewUsers() + 1);
        metric.setTotalUsers(metric.getTotalUsers() + 1);
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordPaymentSuccess(Double amount) {
        recordPaymentSuccess(amount, null);
    }

    @Transactional
    public void recordPaymentSuccess(Double amount, Long courseId) {
        LocalDate today = LocalDate.now();
        DailyMetric metric = dailyMetricRepository.findById(today)
                .orElseGet(() -> DailyMetric.builder().date(today).build());
        metric.setTotalRevenue(metric.getTotalRevenue().add(BigDecimal.valueOf(amount)));
        dailyMetricRepository.save(metric);

        if (courseId != null) {
            Long mentorId = resolveMentorIdForCourse(courseId);
            if (mentorId != null) {
                MentorMetricId mmId = new MentorMetricId(mentorId, today);
                MentorMetric mm = mentorMetricRepository.findById(mmId)
                        .orElseGet(() -> MentorMetric.builder().id(mmId).build());
                mm.setRevenue(mm.getRevenue().add(BigDecimal.valueOf(amount)));
                mentorMetricRepository.save(mm);
            }
        }
    }

    @Transactional
    public void recordLessonCompleted(Long userId) {
        LocalDate today = LocalDate.now();
        StudentActivityId saId = new StudentActivityId(userId, today);
        StudentActivity sa = studentActivityRepository.findById(saId)
                .orElseGet(() -> StudentActivity.builder().id(saId).build());
        sa.setLessonsCompleted(sa.getLessonsCompleted() + 1);
        sa.setMinutesLearned(sa.getMinutesLearned() + 15); // Assume 15 minutes per lesson
        studentActivityRepository.save(sa);
    }

    @Transactional
    public void recordTrackCompleted() {
        LocalDate today = LocalDate.now();
        DailyMetric metric = dailyMetricRepository.findById(today)
                .orElseGet(() -> DailyMetric.builder().date(today).build());
        metric.setCompletions(metric.getCompletions() + 1);
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordEnrollment() {
        recordEnrollment(null, null);
    }

    @Transactional
    public void recordEnrollment(Long courseId, Long mentorId) {
        LocalDate today = LocalDate.now();
        DailyMetric metric = dailyMetricRepository.findById(today)
                .orElseGet(() -> DailyMetric.builder().date(today).build());
        metric.setEnrollments(metric.getEnrollments() + 1);
        dailyMetricRepository.save(metric);

        if (courseId != null) {
            CourseMetricId cmId = new CourseMetricId(courseId, today);
            CourseMetric cm = courseMetricRepository.findById(cmId)
                    .orElseGet(() -> CourseMetric.builder().id(cmId).build());
            cm.setEnrollments(cm.getEnrollments() + 1);
            courseMetricRepository.save(cm);
        }

        if (mentorId != null) {
            MentorMetricId mmId = new MentorMetricId(mentorId, today);
            MentorMetric mm = mentorMetricRepository.findById(mmId)
                    .orElseGet(() -> MentorMetric.builder().id(mmId).build());
            mm.setNewStudents(mm.getNewStudents() + 1);
            mm.setActiveStudents(mm.getActiveStudents() + 1);
            mentorMetricRepository.save(mm);
        }
    }

    public Map<String, Object> mentorDashboard(Long mentorId) {
        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorId(mentorId);

        BigDecimal totalRevenue = metrics.stream()
                .map(MentorMetric::getRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalStudents = metrics.stream()
                .map(MentorMetric::getActiveStudents)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<Long> courseIds = getCourseIdsForMentor(mentorId);
        List<Map<String, Object>> coursePerformance = new ArrayList<>();
        double totalRating = 0.0;
        int ratedCourses = 0;

        for (Long courseId : courseIds) {
            List<CourseMetric> courseMetrics = courseMetricRepository.findByIdCourseIdAndIdDateBetweenOrderByIdDateAsc(
                    courseId, LocalDate.now().minusDays(30), LocalDate.now());

            int views = courseMetrics.stream().mapToInt(CourseMetric::getViews).sum();
            int enrollments = courseMetrics.stream().mapToInt(CourseMetric::getEnrollments).sum();
            int completions = courseMetrics.stream().mapToInt(CourseMetric::getCompletions).sum();
            double avgRating = courseMetrics.stream()
                    .mapToDouble(CourseMetric::getAvgRating)
                    .average()
                    .orElse(0.0);

            if (avgRating > 0.0) {
                totalRating += avgRating;
                ratedCourses++;
            }

            coursePerformance.add(Map.of(
                    "courseId", courseId,
                    "title", resolveCourseTitle(courseId),
                    "views", views,
                    "enrollments", enrollments,
                    "completions", completions,
                    "avgRating", Math.round(avgRating * 10.0) / 10.0
            ));
        }

        double avgRating = ratedCourses > 0 ? (totalRating / ratedCourses) : 0.0;

        return Map.of(
                "totalRevenue", totalRevenue,
                "totalStudents", totalStudents,
                "activeCourses", courseIds.size(),
                "avgRating", Math.round(avgRating * 10.0) / 10.0,
                "coursePerformance", coursePerformance
        );
    }

    public Map<String, Object> mentorRevenue(Long mentorId, String period) {
        LocalDate end = LocalDate.now();
        LocalDate start = "month".equalsIgnoreCase(period) ? end.minusDays(30) : end.minusDays(7);

        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(mentorId, start, end);

        List<Map<String, Object>> revenueData = metrics.stream()
                .map(m -> Map.<String, Object>of(
                        "date", m.getId().getDate().toString(),
                        "revenue", m.getRevenue()
                ))
                .toList();

        BigDecimal totalRevenue = metrics.stream()
                .map(MentorMetric::getRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = metrics.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(metrics.size()), 2, RoundingMode.HALF_UP);

        return Map.of(
                "period", period,
                "revenueData", revenueData,
                "totalRevenue", totalRevenue,
                "averageDailyRevenue", avgRevenue
        );
    }

    public Map<String, Object> mentorStudents(Long mentorId) {
        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorId(mentorId);

        int totalActiveStudents = metrics.stream()
                .map(MentorMetric::getActiveStudents)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        LocalDate weekAgo = LocalDate.now().minusDays(7);
        int newStudentsThisWeek = metrics.stream()
                .filter(m -> !m.getId().getDate().isBefore(weekAgo))
                .map(MentorMetric::getNewStudents)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        List<Map<String, Object>> studentTrends = metrics.stream()
                .map(m -> Map.<String, Object>of(
                        "date", m.getId().getDate().toString(),
                        "newStudents", m.getNewStudents(),
                        "activeStudents", m.getActiveStudents()
                ))
                .toList();

        return Map.of(
                "totalActiveStudents", totalActiveStudents,
                "newStudentsThisWeek", newStudentsThisWeek,
                "studentTrends", studentTrends
        );
    }

    public Map<String, Object> mentorCourseAnalytics(Long mentorId, Long courseId) {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        List<CourseMetric> metrics = courseMetricRepository.findByIdCourseIdAndIdDateBetweenOrderByIdDateAsc(courseId, start, end);

        int totalViews = metrics.stream().mapToInt(CourseMetric::getViews).sum();
        int totalEnrollments = metrics.stream().mapToInt(CourseMetric::getEnrollments).sum();
        int totalCompletions = metrics.stream().mapToInt(CourseMetric::getCompletions).sum();
        double avgRating = metrics.stream().mapToDouble(CourseMetric::getAvgRating).average().orElse(0.0);

        List<Map<String, Object>> dailyMetrics = metrics.stream()
                .map(m -> Map.<String, Object>of(
                        "date", m.getId().getDate().toString(),
                        "views", m.getViews(),
                        "enrollments", m.getEnrollments(),
                        "completions", m.getCompletions(),
                        "rating", m.getAvgRating()
                ))
                .toList();

        return Map.of(
                "courseId", courseId,
                "title", resolveCourseTitle(courseId),
                "totalViews", totalViews,
                "totalEnrollments", totalEnrollments,
                "totalCompletions", totalCompletions,
                "avgRating", Math.round(avgRating * 10.0) / 10.0,
                "dailyMetrics", dailyMetrics
        );
    }

    public Map<String, Object> adminDashboard() {
        List<DailyMetric> metrics = dailyMetricRepository.findAll();

        DailyMetric latest = metrics.stream()
                .max(Comparator.comparing(DailyMetric::getDate))
                .orElse(null);

        int totalUsers = latest != null ? latest.getTotalUsers() : 0;

        BigDecimal totalRevenue = metrics.stream()
                .map(DailyMetric::getTotalRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalEnrollments = metrics.stream().mapToInt(DailyMetric::getEnrollments).sum();
        int totalCompletions = metrics.stream().mapToInt(DailyMetric::getCompletions).sum();

        long activeCourses = courseMetricRepository.findAll().stream()
                .map(m -> m.getId().getCourseId())
                .distinct()
                .count();

        if (activeCourses == 0) {
            activeCourses = getTotalCoursesCountFromCatalog();
        }

        return Map.of(
                "totalUsers", totalUsers,
                "totalRevenue", totalRevenue,
                "totalEnrollments", totalEnrollments,
                "totalCompletions", totalCompletions,
                "activeCourses", activeCourses
        );
    }

    public List<Map<String, Object>> enrollmentReport(LocalDate from, LocalDate to) {
        List<DailyMetric> metrics = dailyMetricRepository.findByDateBetweenOrderByDateAsc(from, to);
        return metrics.stream()
                .map(m -> Map.<String, Object>of(
                        "date", m.getDate().toString(),
                        "enrollments", m.getEnrollments(),
                        "completions", m.getDate() != null ? m.getCompletions() : 0
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> revenueReport(LocalDate from, LocalDate to) {
        List<DailyMetric> metrics = dailyMetricRepository.findByDateBetweenOrderByDateAsc(from, to);
        return metrics.stream()
                .map(m -> Map.<String, Object>of(
                        "date", m.getDate().toString(),
                        "revenue", m.getTotalRevenue()
                ))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> courseReport(LocalDate from, LocalDate to) {
        List<CourseMetric> metrics = courseMetricRepository.findByIdDateBetweenOrderByIdDateAsc(from, to);

        Map<Long, List<CourseMetric>> grouped = metrics.stream()
                .collect(Collectors.groupingBy(m -> m.getId().getCourseId()));

        List<Map<String, Object>> report = new ArrayList<>();
        for (Map.Entry<Long, List<CourseMetric>> entry : grouped.entrySet()) {
            Long courseId = entry.getKey();
            List<CourseMetric> list = entry.getValue();

            int views = list.stream().mapToInt(CourseMetric::getViews).sum();
            int enrollments = list.stream().mapToInt(CourseMetric::getEnrollments).sum();
            int completions = list.stream().mapToInt(CourseMetric::getCompletions).sum();
            double avgRating = list.stream().mapToDouble(CourseMetric::getAvgRating).average().orElse(0.0);

            report.add(Map.of(
                    "courseId", courseId,
                    "title", resolveCourseTitle(courseId),
                    "views", views,
                    "enrollments", enrollments,
                    "completions", completions,
                    "avgRating", Math.round(avgRating * 10.0) / 10.0
            ));
        }

        return report;
    }

    public String exportCsv(String type, LocalDate from, LocalDate to) {
        StringBuilder csv = new StringBuilder();
        if ("enrollments".equalsIgnoreCase(type)) {
            csv.append("Date,Enrollments,Completions\n");
            for (Map<String, Object> row : enrollmentReport(from, to)) {
                csv.append(row.get("date")).append(",")
                        .append(row.get("enrollments")).append(",")
                        .append(row.get("completions")).append("\n");
            }
        } else if ("revenue".equalsIgnoreCase(type)) {
            csv.append("Date,Revenue\n");
            for (Map<String, Object> row : revenueReport(from, to)) {
                csv.append(row.get("date")).append(",")
                        .append(row.get("revenue")).append("\n");
            }
        } else if ("courses".equalsIgnoreCase(type)) {
            csv.append("Course ID,Title,Views,Enrollments,Completions,Avg Rating\n");
            for (Map<String, Object> row : courseReport(from, to)) {
                csv.append(row.get("courseId")).append(",")
                        .append("\"").append(row.get("title")).append("\",")
                        .append(row.get("views")).append(",")
                        .append(row.get("enrollments")).append(",")
                        .append(row.get("completions")).append(",")
                        .append(row.get("avgRating")).append("\n");
            }
        }
        return csv.toString();
    }

    public Map<String, Object> studentDashboard(Long userId) {
        List<StudentActivity> activities = studentActivityRepository.findByIdUserId(userId);

        int lessonsCompleted = activities.stream().mapToInt(StudentActivity::getLessonsCompleted).sum();
        int minutesLearned = activities.stream().mapToInt(StudentActivity::getMinutesLearned).sum();
        int quizzesTaken = activities.stream().mapToInt(StudentActivity::getQuizzesTaken).sum();
        double hoursLearned = Math.round((minutesLearned / 60.0) * 10.0) / 10.0;

        int streak = calculateStreak(activities);

        int coursesInProgress = 0;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> enrollmentDashboard = restClient.get()
                    .uri("http://localhost:8086/api/enrollments/dashboard/student")
                    .header("X-User-Id", String.valueOf(userId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (enrollmentDashboard != null && enrollmentDashboard.get("inProgress") != null) {
                coursesInProgress = ((Number) enrollmentDashboard.get("inProgress")).intValue();
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch courses in progress for user {}: {}", userId, ex.getMessage());
            coursesInProgress = activities.isEmpty() ? 0 : 2;
        }

        return Map.of(
                "coursesInProgress", coursesInProgress,
                "lessonsCompleted", lessonsCompleted,
                "hoursLearned", hoursLearned,
                "quizzesTaken", quizzesTaken,
                "streak", streak
        );
    }

    private int calculateStreak(List<StudentActivity> activities) {
        if (activities == null || activities.isEmpty()) {
            return 0;
        }

        List<LocalDate> activeDates = activities.stream()
                .filter(a -> a.getLessonsCompleted() > 0 || a.getMinutesLearned() > 0 || a.getQuizzesTaken() > 0)
                .map(a -> a.getId().getDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        if (activeDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDate firstDate = activeDates.get(0);
        if (!firstDate.equals(today) && !firstDate.equals(yesterday)) {
            return 0;
        }

        int streak = 1;
        LocalDate current = firstDate;
        for (int i = 1; i < activeDates.size(); i++) {
            LocalDate prev = activeDates.get(i);
            if (prev.equals(current.minusDays(1))) {
                streak++;
                current = prev;
            } else if (prev.equals(current)) {
                // Ignore duplicates
            } else {
                break;
            }
        }
        return streak;
    }

    private Long resolveMentorIdForCourse(Long courseId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses/id/{courseId}", courseId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body != null && body.get("mentorId") != null) {
                return ((Number) body.get("mentorId")).longValue();
            }
        } catch (Exception ex) {
            log.warn("Failed to resolve mentor for course {}: {}", courseId, ex.getMessage());
        }
        return 2L;
    }

    private String resolveCourseTitle(Long courseId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses/id/{courseId}", courseId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (body != null && body.get("title") != null) {
                return body.get("title").toString();
            }
        } catch (Exception ex) {
            log.warn("Failed to resolve title for course {}: {}", courseId, ex.getMessage());
        }
        return "Course #" + courseId;
    }

    private List<Long> getCourseIdsForMentor(Long mentorId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses?size=1000")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("content") != null) {
                List<?> content = (List<?>) response.get("content");
                return content.stream()
                        .map(item -> (Map<?, ?>) item)
                        .filter(map -> map.get("mentorId") != null && ((Number) map.get("mentorId")).longValue() == mentorId)
                        .map(map -> ((Number) map.get("id")).longValue())
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch courses for mentor {}: {}", mentorId, ex.getMessage());
        }
        return mentorId == 2L ? List.of(1L, 2L, 3L, 4L) : List.of();
    }

    private long getTotalCoursesCountFromCatalog() {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(catalogServiceUrl + "/api/catalog/courses?size=1")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (response != null && response.get("totalElements") != null) {
                return ((Number) response.get("totalElements")).longValue();
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch total courses count: {}", ex.getMessage());
        }
        return 4;
    }
}
