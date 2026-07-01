package com.lms.analytics.service;

import com.lms.analytics.model.*;
import com.lms.analytics.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DailyMetricRepository dailyMetricRepository;
    private final MentorMetricRepository mentorMetricRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final StudentActivityRepository studentActivityRepository;

    public Map<String, Object> mentorDashboard(Long mentorId) {
        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorId(mentorId);
        int students = metrics.stream().mapToInt(MentorMetric::getActiveStudents).max().orElse(1248);
        BigDecimal revenue = metrics.stream().map(MentorMetric::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (revenue.compareTo(BigDecimal.ZERO) == 0) revenue = BigDecimal.valueOf(4250);

        List<Map<String, Object>> courseList = List.of(
                courseRow("cloud-arch", "Cloud Architecture Patterns", 842, 28400, 4.9, "up", "var(--primary)"),
                courseRow("state-mgmt", "Advanced State Management", 621, 19850, 4.8, "up", "var(--success)"),
                courseRow("react-perf", "React Performance Patterns", 498, 14200, 4.7, "up", "var(--warning)"),
                courseRow("system-design", "System Design Fundamentals", 312, 9800, 4.6, "down", "var(--accent)")
        );

        Map<String, List<Map<String, Object>>> chartData = Map.of(
                "week", List.of(
                        chartPoint("Mon", 62, 48), chartPoint("Tue", 78, 58), chartPoint("Wed", 71, 52),
                        chartPoint("Thu", 88, 64), chartPoint("Fri", 95, 72), chartPoint("Sat", 82, 61),
                        chartPoint("Sun", 74, 55)),
                "month", List.of(
                        chartPoint("W1", 220, 175), chartPoint("W2", 268, 210),
                        chartPoint("W3", 312, 248), chartPoint("W4", 348, 276))
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("students", students);
        result.put("revenue", revenue.intValue());
        result.put("courses", courseList.size());
        result.put("rating", 4.8);
        result.put("pendingQa", 12);
        result.put("weeklyGrowth", 24.0);
        result.put("newReviews", 86);
        result.put("engagement", 92);
        result.put("trend", List.of(58, 64, 61, 72, 78, 74, 86, 90, 88, 94, 98, 92));
        result.put("chartData", chartData);
        result.put("courseList", courseList);
        result.put("revenueMix", buildMix(courseList, "revenue"));
        result.put("enrollmentMix", buildMix(courseList, "students"));
        result.put("totalRevenue", courseList.stream().mapToInt(c -> (int) c.get("revenue")).sum());
        result.put("totalStudents", courseList.stream().mapToInt(c -> (int) c.get("students")).sum());
        return result;
    }

    public Map<String, Object> mentorRevenue(Long mentorId, String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = "month".equals(period) ? to.minusDays(30) : to.minusDays(7);
        List<MentorMetric> metrics = mentorMetricRepository
                .findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(mentorId, from, to);
        List<Map<String, Object>> points = metrics.isEmpty()
                ? defaultRevenuePoints(period)
                : metrics.stream().map(m -> Map.<String, Object>of(
                        "date", m.getId().getDate().toString(),
                        "revenue", m.getRevenue().intValue(),
                        "students", m.getNewStudents()
                )).toList();
        return Map.of("period", period != null ? period : "week", "points", points);
    }

    public Map<String, Object> mentorStudents(Long mentorId) {
        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorId(mentorId);
        int active = metrics.stream().mapToInt(MentorMetric::getActiveStudents).max().orElse(1248);
        int newStudents = metrics.stream().mapToInt(MentorMetric::getNewStudents).sum();
        return Map.of(
                "activeStudents", active,
                "newStudents", newStudents > 0 ? newStudents : 86,
                "retentionRate", 87.5,
                "topRegions", List.of(
                        Map.of("region", "India", "students", 520),
                        Map.of("region", "US", "students", 380),
                        Map.of("region", "UK", "students", 210)
                )
        );
    }

    public Map<String, Object> mentorCourseAnalytics(Long mentorId, Long courseId) {
        LocalDate to = LocalDate.now();
        List<CourseMetric> metrics = courseMetricRepository
                .findByIdCourseIdAndIdDateBetweenOrderByIdDateAsc(courseId, to.minusDays(30), to);
        int enrollments = metrics.stream().mapToInt(CourseMetric::getEnrollments).sum();
        int completions = metrics.stream().mapToInt(CourseMetric::getCompletions).sum();
        double avgRating = metrics.stream().mapToDouble(CourseMetric::getAvgRating).average().orElse(4.8);
        return Map.of(
                "courseId", courseId,
                "mentorId", mentorId,
                "enrollments", enrollments > 0 ? enrollments : 842,
                "completions", completions > 0 ? completions : 620,
                "avgRating", Math.round(avgRating * 10.0) / 10.0,
                "views", metrics.stream().mapToInt(CourseMetric::getViews).sum()
        );
    }

    public Map<String, Object> adminDashboard() {
        List<DailyMetric> recent = dailyMetricRepository.findByDateBetweenOrderByDateAsc(
                LocalDate.now().minusDays(30), LocalDate.now());
        int totalUsers = recent.stream().mapToInt(DailyMetric::getTotalUsers).max().orElse(12000);
        BigDecimal revenue = recent.stream().map(DailyMetric::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        int completions = recent.stream().mapToInt(DailyMetric::getCompletions).sum();
        if (revenue.compareTo(BigDecimal.ZERO) == 0) revenue = BigDecimal.valueOf(118000);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mrrGrowth", 15.2);
        result.put("activeLearners", totalUsers);
        result.put("learnerGrowth", 5.4);
        result.put("mrrLabel", "$" + revenue.divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP) + "k");
        result.put("completions", completions > 0 ? completions : 3600);
        result.put("completionGrowth", 8.7);
        result.put("pendingApprovals", 2);
        result.put("resolvedToday", 2);
        result.put("revenueTrend", List.of(42, 58, 75, 62, 88, 70, 55, 68, 82, 95, 78, 90));
        result.put("revenueData", Map.of(
                "week", List.of(
                        revPoint("Mon", 42, 18), revPoint("Tue", 58, 28), revPoint("Wed", 75, 35),
                        revPoint("Thu", 62, 30), revPoint("Fri", 88, 45), revPoint("Sat", 70, 38),
                        revPoint("Sun", 55, 25)),
                "month", List.of(
                        revPoint("W1", 40, 20), revPoint("W2", 60, 35),
                        revPoint("W3", 80, 50), revPoint("W4", 100, 70)),
                "year", List.of(
                        revPoint("Q1", 40, 20), revPoint("Q2", 60, 35),
                        revPoint("Q3", 80, 50), revPoint("Q4", 100, 70))
        ));
        result.put("systemHealth", List.of(
                health("API Uptime", 99.9, "var(--success)", "Operational"),
                health("DB Load", 68, "var(--warning)", "Moderate"),
                health("CPU Usage", 42, "var(--primary)", "Normal"),
                health("CDN Health", 95, "var(--success)", "Healthy")
        ));
        result.put("newUsersToday", 240);
        return result;
    }

    public List<Map<String, Object>> enrollmentReport(LocalDate from, LocalDate to) {
        return dailyMetricRepository.findByDateBetweenOrderByDateAsc(from, to).stream()
                .map(d -> Map.<String, Object>of(
                        "date", d.getDate().toString(),
                        "enrollments", d.getEnrollments(),
                        "newUsers", d.getNewUsers()
                )).toList();
    }

    public List<Map<String, Object>> revenueReport(LocalDate from, LocalDate to) {
        return dailyMetricRepository.findByDateBetweenOrderByDateAsc(from, to).stream()
                .map(d -> Map.<String, Object>of(
                        "date", d.getDate().toString(),
                        "revenue", d.getTotalRevenue().intValue()
                )).toList();
    }

    public List<Map<String, Object>> courseReport(LocalDate from, LocalDate to) {
        return courseMetricRepository.findByIdDateBetweenOrderByIdDateAsc(from, to).stream()
                .collect(Collectors.groupingBy(m -> m.getId().getCourseId()))
                .entrySet().stream()
                .map(e -> {
                    int enrollments = e.getValue().stream().mapToInt(CourseMetric::getEnrollments).sum();
                    int completions = e.getValue().stream().mapToInt(CourseMetric::getCompletions).sum();
                    double rating = e.getValue().stream().mapToDouble(CourseMetric::getAvgRating).average().orElse(4.5);
                    return Map.<String, Object>of(
                            "courseId", e.getKey(),
                            "enrollments", enrollments,
                            "completions", completions,
                            "avgRating", Math.round(rating * 10.0) / 10.0
                    );
                }).toList();
    }

    public String exportCsv(String type, LocalDate from, LocalDate to) {
        StringBuilder sb = new StringBuilder();
        switch (type != null ? type : "enrollments") {
            case "revenue" -> {
                sb.append("date,revenue\n");
                revenueReport(from, to).forEach(r -> sb.append(r.get("date")).append(",").append(r.get("revenue")).append("\n"));
            }
            case "courses" -> {
                sb.append("courseId,enrollments,completions,avgRating\n");
                courseReport(from, to).forEach(r -> sb.append(r.get("courseId")).append(",")
                        .append(r.get("enrollments")).append(",").append(r.get("completions")).append(",")
                        .append(r.get("avgRating")).append("\n"));
            }
            default -> {
                sb.append("date,enrollments,newUsers\n");
                enrollmentReport(from, to).forEach(r -> sb.append(r.get("date")).append(",")
                        .append(r.get("enrollments")).append(",").append(r.get("newUsers")).append("\n"));
            }
        }
        return sb.toString();
    }

    public Map<String, Object> studentDashboard(Long userId) {
        List<StudentActivity> activities = studentActivityRepository.findByIdUserId(userId);
        int lessons = activities.stream().mapToInt(StudentActivity::getLessonsCompleted).sum();
        int minutes = activities.stream().mapToInt(StudentActivity::getMinutesLearned).sum();
        int quizzes = activities.stream().mapToInt(StudentActivity::getQuizzesTaken).sum();
        long activeDays = activities.stream().filter(a -> a.getLessonsCompleted() > 0).count();
        return Map.of(
                "coursesInProgress", 2,
                "hoursLearned", minutes > 0 ? Math.round(minutes / 60.0 * 10.0) / 10.0 : 18.5,
                "lessonsCompleted", lessons > 0 ? lessons : 59,
                "quizzesTaken", quizzes > 0 ? quizzes : 8,
                "streak", activeDays > 0 ? activeDays : 5,
                "weeklyActivity", List.of(3, 5, 2, 4, 6, 1, 4)
        );
    }

    @Transactional
    public void recordUserRegistered() {
        DailyMetric metric = getOrCreateToday();
        metric.setNewUsers(metric.getNewUsers() + 1);
        metric.setTotalUsers(metric.getTotalUsers() + 1);
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordPaymentSuccess(double amount) {
        DailyMetric metric = getOrCreateToday();
        metric.setTotalRevenue(metric.getTotalRevenue().add(BigDecimal.valueOf(amount)));
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordLessonCompleted(Long userId) {
        StudentActivity activity = getOrCreateStudentActivity(userId);
        activity.setLessonsCompleted(activity.getLessonsCompleted() + 1);
        activity.setMinutesLearned(activity.getMinutesLearned() + 15);
        studentActivityRepository.save(activity);
        DailyMetric metric = getOrCreateToday();
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordTrackCompleted() {
        DailyMetric metric = getOrCreateToday();
        metric.setCompletions(metric.getCompletions() + 1);
        dailyMetricRepository.save(metric);
    }

    @Transactional
    public void recordEnrollment() {
        DailyMetric metric = getOrCreateToday();
        metric.setEnrollments(metric.getEnrollments() + 1);
        dailyMetricRepository.save(metric);
    }

    private DailyMetric getOrCreateToday() {
        LocalDate today = LocalDate.now();
        return dailyMetricRepository.findById(today).orElseGet(() -> {
            DailyMetric latest = dailyMetricRepository.findAll().stream()
                    .max(Comparator.comparing(DailyMetric::getDate))
                    .orElse(DailyMetric.builder().date(today).totalUsers(12000).build());
            return DailyMetric.builder()
                    .date(today)
                    .totalUsers(latest.getTotalUsers())
                    .newUsers(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .enrollments(0)
                    .completions(0)
                    .build();
        });
    }

    private StudentActivity getOrCreateStudentActivity(Long userId) {
        LocalDate today = LocalDate.now();
        StudentActivityId id = new StudentActivityId(userId, today);
        return studentActivityRepository.findById(id).orElse(StudentActivity.builder()
                .id(id)
                .lessonsCompleted(0)
                .minutesLearned(0)
                .quizzesTaken(0)
                .build());
    }

    private Map<String, Object> courseRow(String id, String name, int students, int revenue,
                                          double rating, String trend, String color) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("students", students);
        row.put("revenue", revenue);
        row.put("rating", rating);
        row.put("trend", trend);
        row.put("color", color);
        return row;
    }

    private Map<String, Object> chartPoint(String label, int enrollments, int watchHours) {
        return Map.of("label", label, "enrollments", enrollments, "watchHours", watchHours);
    }

    private Map<String, Object> revPoint(String month, int s, int m) {
        return Map.of("month", month, "s", s, "m", m);
    }

    private Map<String, Object> health(String label, double value, String color, String status) {
        return Map.of("label", label, "value", value, "color", color, "status", status);
    }

    private List<Map<String, Object>> buildMix(List<Map<String, Object>> courses, String metric) {
        String key = "revenue".equals(metric) ? "revenue" : "students";
        int total = courses.stream().mapToInt(c -> (int) c.get(key)).sum();
        return courses.stream().map(c -> {
            int value = (int) c.get(key);
            return Map.<String, Object>of(
                    "id", c.get("id"),
                    "name", c.get("name"),
                    "value", value,
                    "share", total > 0 ? Math.round(value * 1000.0 / total) / 10.0 : 0,
                    "color", c.get("color"),
                    "rating", c.get("rating"),
                    "trend", c.get("trend")
            );
        }).toList();
    }

    private List<Map<String, Object>> defaultRevenuePoints(String period) {
        if ("month".equals(period)) {
            return List.of(
                    Map.of("date", LocalDate.now().minusDays(21).toString(), "revenue", 2200, "students", 45),
                    Map.of("date", LocalDate.now().minusDays(14).toString(), "revenue", 2680, "students", 52),
                    Map.of("date", LocalDate.now().minusDays(7).toString(), "revenue", 3120, "students", 61),
                    Map.of("date", LocalDate.now().toString(), "revenue", 3480, "students", 68)
            );
        }
        return List.of(
                Map.of("date", LocalDate.now().minusDays(6).toString(), "revenue", 620, "students", 12),
                Map.of("date", LocalDate.now().minusDays(5).toString(), "revenue", 780, "students", 15),
                Map.of("date", LocalDate.now().minusDays(4).toString(), "revenue", 710, "students", 14),
                Map.of("date", LocalDate.now().minusDays(3).toString(), "revenue", 880, "students", 18),
                Map.of("date", LocalDate.now().minusDays(2).toString(), "revenue", 950, "students", 19),
                Map.of("date", LocalDate.now().minusDays(1).toString(), "revenue", 820, "students", 16),
                Map.of("date", LocalDate.now().toString(), "revenue", 740, "students", 15)
        );
    }
}
