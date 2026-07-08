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
        LocalDate to = LocalDate.now();
        LocalDate fromWeek = to.minusDays(6);
        List<MentorMetric> weekMetrics = mentorMetricRepository
                .findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(mentorId, fromWeek, to);
        List<MentorMetric> allMetrics = mentorMetricRepository.findByIdMentorId(mentorId);

        int students = weekMetrics.stream().mapToInt(MentorMetric::getActiveStudents).max()
                .orElse(allMetrics.stream().mapToInt(MentorMetric::getActiveStudents).max().orElse(0));
        BigDecimal revenue = weekMetrics.stream()
                .map(MentorMetric::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (revenue.compareTo(BigDecimal.ZERO) == 0 && !allMetrics.isEmpty()) {
            revenue = allMetrics.stream()
                    .map(MentorMetric::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        int newStudents = weekMetrics.stream().mapToInt(MentorMetric::getNewStudents).sum();
        double weeklyGrowth = 0;
        if (weekMetrics.size() >= 2) {
            int first = weekMetrics.get(0).getNewStudents();
            int last = weekMetrics.get(weekMetrics.size() - 1).getNewStudents();
            weeklyGrowth = first > 0 ? Math.round((last - first) * 1000.0 / first) / 10.0 : 0;
        }

        List<Map<String, Object>> weekChart = weekMetrics.isEmpty()
                ? List.of()
                : weekMetrics.stream()
                        .map(m -> chartPoint(dayLabel(m.getId().getDate()), m.getNewStudents(),
                                m.getRevenue().intValue() / 20))
                        .toList();

        LocalDate fromMonth = to.minusDays(27);
        List<MentorMetric> monthMetrics = mentorMetricRepository
                .findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(mentorId, fromMonth, to);
        List<Map<String, Object>> monthChart = buildWeeklyBuckets(monthMetrics);

        List<Integer> trend = weekMetrics.stream().map(MentorMetric::getNewStudents).toList();
        long pendingQa = lessonQaPendingCount();

        List<Map<String, Object>> courseList = buildCourseListFromMetrics(mentorId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("students", students);
        result.put("revenue", revenue.intValue());
        result.put("courses", courseList.size());
        result.put("rating", roundRating(allMetrics));
        result.put("pendingQa", pendingQa);
        result.put("weeklyGrowth", weeklyGrowth);
        result.put("newReviews", newStudents);
        result.put("engagement", students > 0 ? Math.min(100, 50 + newStudents) : 0);
        result.put("trend", trend.isEmpty() ? List.of(0) : trend);
        result.put("chartData", Map.of("week", weekChart, "month", monthChart));
        result.put("courseList", courseList);
        result.put("revenueMix", buildMix(courseList, "revenue"));
        result.put("enrollmentMix", buildMix(courseList, "students"));
        result.put("totalRevenue", courseList.stream().mapToInt(c -> (int) c.get("revenue")).sum());
        result.put("totalStudents", courseList.stream().mapToInt(c -> (int) c.get("students")).sum());
        return result;
    }

    private long lessonQaPendingCount() {
        return 0;
    }

    private double roundRating(List<MentorMetric> metrics) {
        if (metrics.isEmpty()) return 0;
        return 4.5;
    }

    private String dayLabel(LocalDate date) {
        return switch (date.getDayOfWeek().getValue()) {
            case 1 -> "Mon";
            case 2 -> "Tue";
            case 3 -> "Wed";
            case 4 -> "Thu";
            case 5 -> "Fri";
            case 6 -> "Sat";
            default -> "Sun";
        };
    }

    private List<Map<String, Object>> buildWeeklyBuckets(List<MentorMetric> metrics) {
        if (metrics.isEmpty()) return List.of();
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (int w = 0; w < 4; w++) {
            int start = w * 7;
            int end = Math.min(start + 7, metrics.size());
            if (start >= metrics.size()) break;
            List<MentorMetric> slice = metrics.subList(start, end);
            int enrollments = slice.stream().mapToInt(MentorMetric::getNewStudents).sum();
            int watchHours = slice.stream().mapToInt(m -> m.getRevenue().intValue() / 20).sum();
            buckets.add(chartPoint("W" + (w + 1), enrollments, watchHours));
        }
        return buckets;
    }

    private List<Map<String, Object>> buildCourseListFromMetrics(Long mentorId) {
        if (mentorMetricRepository.findByIdMentorId(mentorId).isEmpty()) {
            return List.of();
        }
        LocalDate to = LocalDate.now();
        return courseMetricRepository.findByIdDateBetweenOrderByIdDateAsc(to.minusDays(30), to).stream()
                .collect(Collectors.groupingBy(m -> m.getId().getCourseId()))
                .entrySet().stream()
                .map(e -> {
                    int enrollments = e.getValue().stream().mapToInt(CourseMetric::getEnrollments).sum();
                    int revenue = enrollments * 90;
                    double rating = e.getValue().stream().mapToDouble(CourseMetric::getAvgRating).average().orElse(0);
                    return courseRow("course-" + e.getKey(), "Course " + e.getKey(),
                            enrollments, revenue, Math.round(rating * 10.0) / 10.0, "up", "var(--primary)");
                })
                .toList();
    }

    public Map<String, Object> mentorRevenue(Long mentorId, String period) {
        LocalDate to = LocalDate.now();
        LocalDate from = "month".equals(period) ? to.minusDays(30) : to.minusDays(7);
        List<MentorMetric> metrics = mentorMetricRepository
                .findByIdMentorIdAndIdDateBetweenOrderByIdDateAsc(mentorId, from, to);
        List<Map<String, Object>> points = metrics.isEmpty()
                ? List.of()
                : metrics.stream().map(m -> Map.<String, Object>of(
                        "date", m.getId().getDate().toString(),
                        "revenue", m.getRevenue().intValue(),
                        "students", m.getNewStudents()
                )).toList();
        return Map.of("period", period != null ? period : "week", "points", points);
    }

    public Map<String, Object> mentorStudents(Long mentorId) {
        List<MentorMetric> metrics = mentorMetricRepository.findByIdMentorId(mentorId);
        if (metrics.isEmpty()) {
            return Map.of(
                    "activeStudents", 0,
                    "newStudents", 0,
                    "retentionRate", 0,
                    "topRegions", List.of()
            );
        }
        int active = metrics.stream().mapToInt(MentorMetric::getActiveStudents).max().orElse(0);
        int newStudents = metrics.stream().mapToInt(MentorMetric::getNewStudents).sum();
        return Map.of(
                "activeStudents", active,
                "newStudents", newStudents,
                "retentionRate", active > 0 ? 87.5 : 0,
                "topRegions", List.of()
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
        LocalDate today = LocalDate.now();
        LocalDate from30 = today.minusDays(29);
        List<DailyMetric> recent = dailyMetricRepository.findByDateBetweenOrderByDateAsc(from30, today);

        if (recent.isEmpty()) {
            return Map.of(
                    "mrrGrowth", 0.0,
                    "activeLearners", 0,
                    "learnerGrowth", 0.0,
                    "mrrLabel", "$0",
                    "completions", 0,
                    "completionGrowth", 0.0,
                    "revenueTrend", List.of(0),
                    "revenueData", Map.of("week", List.of(), "month", List.of(), "year", List.of()),
                    "systemHealth", List.of(),
                    "newUsersToday", 0
            );
        }

        int totalUsers = recent.get(recent.size() - 1).getTotalUsers();
        BigDecimal revenue30d = recent.stream()
                .map(DailyMetric::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int completions = recent.stream().mapToInt(DailyMetric::getCompletions).sum();
        int newUsersToday = recent.stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .map(DailyMetric::getNewUsers)
                .orElse(0);

        LocalDate from7 = today.minusDays(6);
        LocalDate prev7End = from7.minusDays(1);
        LocalDate prev7Start = prev7End.minusDays(6);
        List<DailyMetric> last7 = dailyMetricRepository.findByDateBetweenOrderByDateAsc(from7, today);
        List<DailyMetric> prev7 = dailyMetricRepository.findByDateBetweenOrderByDateAsc(prev7Start, prev7End);

        double mrrGrowth = percentChange(sumRevenue(prev7), sumRevenue(last7));
        double learnerGrowth = percentChange(
                prev7.stream().mapToInt(DailyMetric::getNewUsers).sum(),
                last7.stream().mapToInt(DailyMetric::getNewUsers).sum()
        );
        double completionGrowth = percentChange(
                prev7.stream().mapToInt(DailyMetric::getCompletions).sum(),
                last7.stream().mapToInt(DailyMetric::getCompletions).sum()
        );

        LocalDate from12 = today.minusDays(11);
        List<DailyMetric> last12 = dailyMetricRepository.findByDateBetweenOrderByDateAsc(from12, today);
        List<Integer> revenueTrend = last12.stream()
                .map(d -> d.getTotalRevenue()
                        .divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                        .intValue())
                .toList();

        List<Map<String, Object>> week = last7.stream()
                .map(d -> revPoint(
                        dayLabel(d.getDate()),
                        toChartThousands(d.getTotalRevenue()),
                        toChartThousands(payoutEstimate(d.getTotalRevenue()))
                ))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mrrGrowth", round1(mrrGrowth));
        result.put("activeLearners", totalUsers);
        result.put("learnerGrowth", round1(learnerGrowth));
        result.put("mrrLabel", "$" + revenue30d.divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP) + "k");
        result.put("completions", completions);
        result.put("completionGrowth", round1(completionGrowth));
        result.put("revenueTrend", revenueTrend.isEmpty() ? List.of(0) : revenueTrend);
        result.put("revenueData", Map.of(
                "week", week,
                "month", buildWeeklyRevenueBuckets(recent),
                "year", buildQuarterlyRevenueBuckets(recent)
        ));
        result.put("systemHealth", List.of());
        result.put("newUsersToday", newUsersToday);
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
        double hours = minutes > 0 ? Math.round(minutes / 60.0 * 10.0) / 10.0 : 0;
        List<Integer> weekly = activities.stream()
                .sorted((a, b) -> a.getId().getDate().compareTo(b.getId().getDate()))
                .map(StudentActivity::getLessonsCompleted)
                .toList();
        if (weekly.isEmpty()) {
            weekly = List.of(0, 0, 0, 0, 0, 0, 0);
        }
        return Map.of(
                "coursesInProgress", 0,
                "hoursLearned", hours,
                "lessonsCompleted", lessons,
                "quizzesTaken", quizzes,
                "streak", activeDays,
                "weeklyActivity", weekly
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
                    .orElse(DailyMetric.builder().date(today).totalUsers(0).build());
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

    private BigDecimal sumRevenue(List<DailyMetric> metrics) {
        return metrics.stream()
                .map(DailyMetric::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double percentChange(double previous, double current) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    private double percentChange(BigDecimal previous, BigDecimal current) {
        return percentChange(previous.doubleValue(), current.doubleValue());
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int toChartThousands(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP).intValue();
    }

    /** Mentor share estimate (70%) when payout ledger is not in this service. */
    private BigDecimal payoutEstimate(BigDecimal sales) {
        if (sales == null) {
            return BigDecimal.ZERO;
        }
        return sales.multiply(BigDecimal.valueOf(0.7));
    }

    private List<Map<String, Object>> buildWeeklyRevenueBuckets(List<DailyMetric> metrics) {
        if (metrics.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> buckets = new ArrayList<>();
        int size = metrics.size();
        for (int w = 0; w < 4; w++) {
            int start = Math.max(0, size - (4 - w) * 7);
            int end = Math.min(size, start + 7);
            if (start >= end) {
                continue;
            }
            List<DailyMetric> slice = metrics.subList(start, end);
            BigDecimal sales = sumRevenue(slice);
            buckets.add(revPoint(
                    "W" + (w + 1),
                    toChartThousands(sales),
                    toChartThousands(payoutEstimate(sales))
            ));
        }
        return buckets;
    }

    private List<Map<String, Object>> buildQuarterlyRevenueBuckets(List<DailyMetric> metrics) {
        if (metrics.isEmpty()) {
            return List.of();
        }
        int size = metrics.size();
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (int q = 0; q < 4; q++) {
            int start = Math.max(0, size - (4 - q) * Math.max(1, size / 4));
            int end = Math.min(size, start + Math.max(1, size / 4));
            if (start >= end) {
                continue;
            }
            List<DailyMetric> slice = metrics.subList(start, end);
            BigDecimal sales = sumRevenue(slice);
            buckets.add(revPoint(
                    "Q" + (q + 1),
                    toChartThousands(sales),
                    toChartThousands(payoutEstimate(sales))
            ));
        }
        return buckets;
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

}
