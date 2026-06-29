package com.lms.mentor.dto;

import com.lms.mentor.model.Mentor;
import com.lms.mentor.model.MentorExperience;
import com.lms.mentor.model.MentorTaughtCourse;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class MentorResponse {
    String slug;
    String name;
    String role;
    String company;
    String trackLabel;
    String avatar;
    String bio;
    String longBio;
    List<String> specialties;
    Integer courses;
    String learners;
    Double rating;
    Integer reviews;
    Integer sessions;
    Integer yearsExp;
    String location;
    Boolean available;
    List<String> achievements;
    List<Map<String, String>> experience;
    List<Map<String, Object>> taughtCourses;

    public static MentorResponse from(Mentor mentor) {
        return MentorResponse.builder()
                .slug(mentor.getSlug())
                .name(mentor.getName())
                .role(mentor.getRole())
                .company(mentor.getCompany())
                .trackLabel(mentor.getTrackLabel())
                .avatar(mentor.getAvatarUrl())
                .bio(mentor.getBio())
                .longBio(mentor.getLongBio())
                .specialties(mentor.getSpecialties() == null ? List.of() : new ArrayList<>(mentor.getSpecialties()))
                .courses(mentor.getTaughtCourses().size())
                .learners(mentor.getLearnersCount())
                .rating(mentor.getRating())
                .reviews(mentor.getReviewsCount())
                .sessions(mentor.getSessionsCount())
                .yearsExp(mentor.getYearsExp())
                .location(mentor.getLocation())
                .available(mentor.getAvailable())
                .achievements(mentor.getAchievements() == null ? List.of() : new ArrayList<>(mentor.getAchievements()))
                .experience(mentor.getExperience().stream()
                        .map(MentorResponse::mapExperience).toList())
                .taughtCourses(mentor.getTaughtCourses().stream()
                        .map(MentorResponse::mapTaughtCourse).toList())
                .build();
    }

    public static MentorResponse summary(Mentor mentor) {
        return MentorResponse.builder()
                .slug(mentor.getSlug())
                .name(mentor.getName())
                .role(mentor.getRole())
                .company(mentor.getCompany())
                .trackLabel(mentor.getTrackLabel())
                .avatar(mentor.getAvatarUrl())
                .bio(mentor.getBio())
                .rating(mentor.getRating())
                .reviews(mentor.getReviewsCount())
                .learners(mentor.getLearnersCount())
                .available(mentor.getAvailable())
                .specialties(mentor.getSpecialties() == null ? List.of() : new ArrayList<>(mentor.getSpecialties()))
                .build();
    }

    private static Map<String, String> mapExperience(MentorExperience exp) {
        return Map.of(
                "title", exp.getTitle(),
                "org", exp.getOrg(),
                "period", exp.getPeriod(),
                "text", exp.getDescription()
        );
    }

    private static Map<String, Object> mapTaughtCourse(MentorTaughtCourse course) {
        return Map.of(
                "title", course.getTitle(),
                "level", course.getLevel(),
                "modules", course.getModules(),
                "hours", course.getHours()
        );
    }
}
