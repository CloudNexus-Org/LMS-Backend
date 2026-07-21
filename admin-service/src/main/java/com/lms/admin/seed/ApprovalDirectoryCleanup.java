package com.lms.admin.seed;

import com.lms.admin.client.UserClient;
import com.lms.admin.model.CourseApproval;
import com.lms.admin.repository.CourseApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Removes approvals for inactive mentors — OFF by default. */
@Component
@ConditionalOnProperty(name = "lms.directory.cleanup.enabled", havingValue = "true")
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ApprovalDirectoryCleanup implements CommandLineRunner {

    private final CourseApprovalRepository courseApprovalRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public void run(String... args) {
        List<CourseApproval> toRemove = new ArrayList<>();
        for (CourseApproval approval : courseApprovalRepository.findAll()) {
            if (shouldRemove(approval)) {
                toRemove.add(approval);
            }
        }
        if (!toRemove.isEmpty()) {
            courseApprovalRepository.deleteAll(toRemove);
            log.info("Approval cleanup removed {} static/inactive submission(s)", toRemove.size());
        }
    }

    private boolean shouldRemove(CourseApproval approval) {
        String title = approval.getTitle() != null ? approval.getTitle().trim() : "";
        if (title.startsWith("Verification Test")) {
            return true;
        }
        String description = approval.getDescription() != null ? approval.getDescription() : "";
        if (description.contains("automated API verification")) {
            return true;
        }
        Long mentorId = approval.getMentorId();
        if (mentorId == null) {
            return true;
        }
        return !userClient.isActiveMentor(mentorId);
    }
}
