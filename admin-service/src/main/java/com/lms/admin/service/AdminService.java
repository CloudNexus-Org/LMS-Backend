package com.lms.admin.service;

import com.lms.admin.dto.*;
import com.lms.admin.event.AdminEventProducer;
import com.lms.admin.model.*;
import com.lms.admin.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private static final String ADMIN_ROLE = "admin";
    private static final long TOTAL_SALES = 1_248_000L;
    private static final long MENTOR_PAYOUTS_TOTAL = 819_500L;
    private static final long NET_REVENUE = 428_500L;

    private final CourseApprovalRepository courseApprovalRepository;
    private final PlatformSettingRepository platformSettingRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final MentorPayoutRepository mentorPayoutRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AdminEventProducer eventProducer;

    @Value("${lms.catalog-service-url}")
    private String catalogServiceUrl;

    private final RestClient restClient = RestClient.create();

    public void requireAdmin(String role) {
        if (role == null || !ADMIN_ROLE.equalsIgnoreCase(role.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }

    public List<CourseApprovalResponse> listApprovals(String status) {
        List<CourseApproval> approvals;
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            approvals = courseApprovalRepository.findAll();
            approvals.sort(Comparator.comparing(CourseApproval::getSubmittedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            approvals = courseApprovalRepository.findByStatusIgnoreCaseOrderBySubmittedAtDesc(status);
        }
        return approvals.stream().map(CourseApprovalResponse::from).toList();
    }

    public CourseApprovalResponse getApproval(String courseId) {
        return CourseApprovalResponse.from(findApproval(courseId));
    }

    @Transactional
    public void createPendingApproval(Map<String, Object> event) {
        String courseCode = stringVal(event.get("courseCode"));
        if (courseCode == null || courseCode.isBlank()) {
            log.warn("course.submitted missing courseCode: {}", event);
            return;
        }
        if (courseApprovalRepository.existsById(courseCode)) {
            log.info("Approval already exists for {}", courseCode);
            return;
        }

        CourseApproval approval = CourseApproval.builder()
                .courseId(courseCode)
                .title(stringVal(event.get("title")))
                .mentor(stringVal(event.get("mentorName")))
                .mentorAvatar(stringVal(event.get("mentorAvatar")))
                .category(stringVal(event.get("category")))
                .submitted("Just now")
                .modules(intVal(event.get("modules")))
                .lessons(intVal(event.get("lessons")))
                .duration(stringVal(event.get("duration")))
                .previewRating(0.0)
                .thumbnail(stringVal(event.get("thumbnail")))
                .status("Pending")
                .priority(stringVal(event.get("priority")))
                .description(stringVal(event.get("description")))
                .mentorId(longVal(event.get("mentorId")))
                .submittedAt(Instant.now())
                .build();

        courseApprovalRepository.save(approval);
        log.info("Created pending approval for course {}", courseCode);
    }

    @Transactional
    public CourseApprovalResponse approveCourse(String courseId, Long adminId) {
        CourseApproval approval = findApproval(courseId);
        if (!"Pending".equalsIgnoreCase(approval.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course is not pending approval");
        }
        approval.setStatus("Approved");
        approval.setReviewedAt(Instant.now());
        approval.setReviewedBy(adminId);
        approval.setRejectionReason(null);
        courseApprovalRepository.save(approval);

        Long numericCourseId = parseNumericCourseId(courseId);
        syncPublishToCatalog(numericCourseId);
        eventProducer.publishCourseApproved(courseId, numericCourseId, approval.getMentorId(), approval.getTitle());
        audit(adminId, "APPROVE_COURSE", "course_approval", courseId,
                "Approved course: " + approval.getTitle());

        return CourseApprovalResponse.from(approval);
    }

    @Transactional
    public CourseApprovalResponse rejectCourse(String courseId, Long adminId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }
        CourseApproval approval = findApproval(courseId);
        if (!"Pending".equalsIgnoreCase(approval.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course is not pending approval");
        }
        approval.setStatus("Rejected");
        approval.setReviewedAt(Instant.now());
        approval.setReviewedBy(adminId);
        approval.setRejectionReason(reason.trim());
        courseApprovalRepository.save(approval);

        eventProducer.publishCourseRejected(courseId, approval.getMentorId(), approval.getTitle(), reason.trim());
        audit(adminId, "REJECT_COURSE", "course_approval", courseId,
                "Rejected course: " + approval.getTitle() + " — " + reason.trim());

        return CourseApprovalResponse.from(approval);
    }

    public FinancialSummaryResponse getFinancialSummary() {
        int recentSales = (int) financialTransactionRepository.findAll().stream()
                .filter(tx -> "Course Sale".equals(tx.getType()))
                .count();
        String currency = platformSettingRepository.findById("platform.currency")
                .map(PlatformSetting::getValue)
                .orElse("INR");

        return FinancialSummaryResponse.builder()
                .totalSales(TOTAL_SALES)
                .mentorPayouts(MENTOR_PAYOUTS_TOTAL)
                .netRevenue(NET_REVENUE)
                .platformCut(TOTAL_SALES - MENTOR_PAYOUTS_TOTAL)
                .recentSalesCount(recentSales)
                .currency(currency)
                .build();
    }

    public List<TransactionResponse> listTransactions(String type, String search) {
        Specification<FinancialTransaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null && !type.isBlank() && !"all".equalsIgnoreCase(type)) {
                predicates.add(cb.equal(cb.lower(root.get("type")), type.toLowerCase()));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("id")), pattern),
                        cb.like(cb.lower(root.get("student")), pattern),
                        cb.like(cb.lower(root.get("course")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        return financialTransactionRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(FinancialTransaction::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(TransactionResponse::from)
                .toList();
    }

    public List<MentorPayoutResponse> listPayouts() {
        return mentorPayoutRepository.findAllByOrderByProcessedAtDesc().stream()
                .map(MentorPayoutResponse::from)
                .toList();
    }

    @Transactional
    public MentorPayoutResponse processPayout(ProcessPayoutRequest request, Long adminId) {
        if (request.getMentorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mentorId is required");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }
        String id = "PO-" + (4000 + mentorPayoutRepository.count() + 1);
        MentorPayout payout = MentorPayout.builder()
                .id(id)
                .mentorId(request.getMentorId())
                .mentorName(request.getMentorName())
                .amount(request.getAmount())
                .period(request.getPeriod() != null ? request.getPeriod() : "Monthly payout")
                .status("Processed")
                .processedAt(Instant.now())
                .build();
        mentorPayoutRepository.save(payout);

        FinancialTransaction tx = FinancialTransaction.builder()
                .id(id)
                .type("Mentor Payout")
                .amount(request.getAmount().negate())
                .platformCut(null)
                .userId(request.getMentorId())
                .referenceId(id)
                .student(request.getMentorName())
                .course("Monthly payout")
                .dateLabel("Just now")
                .createdAt(Instant.now())
                .build();
        financialTransactionRepository.save(tx);

        audit(adminId, "PROCESS_PAYOUT", "mentor_payout", id,
                "Processed payout of " + request.getAmount() + " for mentor " + request.getMentorId());

        return MentorPayoutResponse.from(payout);
    }

    public SettingsResponse getSettings() {
        Map<String, String> settings = platformSettingRepository.findAll().stream()
                .collect(Collectors.toMap(PlatformSetting::getKey, PlatformSetting::getValue));
        return SettingsResponse.builder().settings(settings).build();
    }

    @Transactional
    public SettingsResponse updateSettings(UpdateSettingsRequest request, Long adminId) {
        Map<String, String> updates = new java.util.LinkedHashMap<>();
        if (request.getSettings() != null && !request.getSettings().isEmpty()) {
            updates.putAll(request.getSettings());
        }
        if (request.getPlatformName() != null) {
            updates.put("platform.name", request.getPlatformName());
        }
        if (request.getCommissionPct() != null) {
            updates.put("platform.commission_pct", String.valueOf(request.getCommissionPct() / 100.0));
        }
        if (request.getGstRate() != null) {
            updates.put("platform.gst_rate", String.valueOf(request.getGstRate()));
        }
        if (request.getCurrency() != null) {
            updates.put("platform.currency", request.getCurrency());
        }
        if (request.getSupportEmail() != null) {
            updates.put("platform.support_email", request.getSupportEmail());
        }
        if (updates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Settings payload is required");
        }
        Instant now = Instant.now();
        updates.forEach((key, value) -> {
            PlatformSetting setting = platformSettingRepository.findById(key)
                    .orElse(PlatformSetting.builder().key(key).build());
            setting.setValue(value);
            setting.setUpdatedAt(now);
            setting.setUpdatedBy(adminId);
            platformSettingRepository.save(setting);
        });
        audit(adminId, "UPDATE_SETTINGS", "platform_settings", "bulk",
                "Updated keys: " + String.join(", ", updates.keySet()));
        return getSettings();
    }

    public PlatformReportResponse getPlatformReport() {
        return buildPlatformReport(1.0);
    }

    public PlatformReportResponse generateReport(GenerateReportRequest request) {
        double multiplier = switch (request.getPeriod() != null ? request.getPeriod().toLowerCase() : "year") {
            case "month" -> 0.18;
            case "quarter" -> 0.42;
            default -> 1.0;
        };
        return buildPlatformReport(multiplier);
    }

    private PlatformReportResponse buildPlatformReport(double multiplier) {
        Map<String, Object> kpis = Map.of(
                "revenue", round(428.5 * multiplier),
                "users", (int) Math.round(12482 * multiplier),
                "courses", (int) Math.round(284 * multiplier),
                "completion", (int) Math.round(63 * multiplier)
        );

        List<Map<String, Object>> topCourses = List.of(
                courseRank(1, "AWS Cloud Architect Pro", "Sarah Chen", 2840, 28400, 4.9, "+22%", true),
                courseRank(2, "Kubernetes & DevOps Mastery", "Liam Carter", 2210, 22100, 4.8, "+18%", true),
                courseRank(3, "React & Next.js Complete", "Priya Nair", 1985, 19850, 4.7, "+15%", true),
                courseRank(4, "Python for Data Science", "Omar Hassan", 1740, 17400, 4.6, "-3%", false),
                courseRank(5, "System Design at Scale", "Yuki Tanaka", 1320, 13200, 4.8, "+12%", true)
        );

        List<Map<String, Object>> topMentors = List.of(
                mentorRank("Sarah Chen", 4, 5840, 58400, 4.9, "SC", "from-blue-500 to-cyan-400"),
                mentorRank("Liam Carter", 3, 4210, 42100, 4.8, "LC", "from-violet-500 to-fuchsia-400"),
                mentorRank("Priya Nair", 5, 3985, 39850, 4.7, "PN", "from-emerald-500 to-lime-400")
        );

        List<Map<String, Object>> categories = List.of(
                categoryShare("Cloud & DevOps", 45, "bg-primary", "text-primary"),
                categoryShare("Frontend Engineering", 30, "bg-success", "text-success"),
                categoryShare("Backend & Systems", 15, "bg-warning", "text-warning"),
                categoryShare("Data & AI", 10, "bg-accent", "text-accent")
        );

        List<Map<String, Object>> geoData = List.of(
                geoRegion("North America", 42, "bg-primary"),
                geoRegion("Europe", 28, "bg-accent"),
                geoRegion("Asia Pacific", 20, "bg-success"),
                geoRegion("Rest of World", 10, "bg-warning")
        );

        return PlatformReportResponse.builder()
                .kpis(kpis)
                .topCourses(topCourses)
                .topMentors(topMentors)
                .categories(categories)
                .geoData(geoData)
                .build();
    }

    private CourseApproval findApproval(String courseId) {
        return courseApprovalRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course approval not found"));
    }

    private Long parseNumericCourseId(String courseId) {
        String digits = courseId.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return null;
        }
        return Long.parseLong(digits);
    }

    private void syncPublishToCatalog(Long courseId) {
        if (courseId == null) {
            log.warn("Cannot sync publish — missing numeric course id");
            return;
        }
        try {
            restClient.post()
                    .uri(catalogServiceUrl + "/api/catalog/internal/courses/" + courseId + "/publish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Synced publish to catalog for course id={}", courseId);
        } catch (Exception ex) {
            log.warn("Catalog sync publish failed (Kafka consumer may still process): {}", ex.getMessage());
        }
    }

    private void audit(Long adminId, String action, String entity, String entityId, String details) {
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .adminId(adminId)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .createdAt(Instant.now())
                .build());
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static Map<String, Object> courseRank(int rank, String name, String mentor, int students,
                                                  int revenue, double rating, String growth, boolean up) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("rank", rank);
        row.put("name", name);
        row.put("mentor", mentor);
        row.put("students", students);
        row.put("revenue", "$" + String.format("%,d", revenue));
        row.put("rating", rating);
        row.put("growth", growth);
        row.put("up", up);
        return row;
    }

    private static Map<String, Object> mentorRank(String name, int courses, int students,
                                                    int revenue, double rating, String avatar, String grad) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("courses", courses);
        row.put("students", students);
        row.put("revenue", "$" + String.format("%,d", revenue));
        row.put("rating", rating);
        row.put("avatar", avatar);
        row.put("grad", grad);
        return row;
    }

    private static Map<String, Object> categoryShare(String name, int share, String color, String text) {
        return Map.of("name", name, "share", share, "color", color, "text", text);
    }

    private static Map<String, Object> geoRegion(String region, int pct, String color) {
        return Map.of("region", region, "pct", pct, "color", color);
    }

    private static String stringVal(Object value) {
        return value != null ? value.toString() : null;
    }

    private static Integer intVal(Object value) {
        if (value == null) return 0;
        return Integer.valueOf(value.toString());
    }

    private static Long longVal(Object value) {
        if (value == null) return null;
        return Long.valueOf(value.toString());
    }
}
