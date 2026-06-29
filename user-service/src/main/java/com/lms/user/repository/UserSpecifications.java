package com.lms.user.repository;

import com.lms.user.model.User;
import com.lms.user.model.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> adminFilter(String search, String role, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.notEqual(root.get("status"), UserStatus.DELETED));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }

            if (role != null && !role.isBlank() && !"All".equalsIgnoreCase(role)) {
                predicates.add(cb.equal(cb.lower(root.get("role")), normalizeRole(role)));
            }

            if (status != null && !status.isBlank() && !"All".equalsIgnoreCase(status)) {
                predicates.add(cb.equal(root.get("status"), parseStatus(status)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static UserStatus parseStatus(String status) {
        return UserStatus.valueOf(status.trim().toUpperCase());
    }

    private static String normalizeRole(String role) {
        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> "admin";
            case "MENTOR" -> "mentor";
            default -> "student";
        };
    }
}
