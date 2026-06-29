package com.lms.mentor.service;

import com.lms.mentor.dto.MentorProfileUpdateRequest;
import com.lms.mentor.dto.MentorResponse;
import com.lms.mentor.event.MentorEventProducer;
import com.lms.mentor.model.Mentor;
import com.lms.mentor.model.MentorStudent;
import com.lms.mentor.repository.MentorRepository;
import com.lms.mentor.repository.MentorStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorService {

    private final MentorRepository mentorRepository;
    private final MentorStudentRepository mentorStudentRepository;
    private final MentorEventProducer eventProducer;

    public List<MentorResponse> listMentors() {
        return mentorRepository.findAll().stream()
                .map(MentorResponse::summary)
                .toList();
    }

    public MentorResponse getBySlug(String slug) {
        Mentor mentor = mentorRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));
        return MentorResponse.from(mentor);
    }

    public List<Map<String, Object>> getMentorCourses(String slug) {
        Mentor mentor = mentorRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));
        return mentor.getTaughtCourses().stream()
                .map(tc -> Map.<String, Object>of(
                        "courseId", tc.getCourseId(),
                        "title", tc.getTitle(),
                        "level", tc.getLevel(),
                        "modules", tc.getModules(),
                        "hours", tc.getHours()))
                .toList();
    }

    public Map<String, Object> getMentorReviewsSummary(String slug) {
        Mentor mentor = mentorRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));
        return Map.of(
                "slug", mentor.getSlug(),
                "rating", mentor.getRating(),
                "reviews", mentor.getReviewsCount()
        );
    }

    public Map<String, Object> getDashboard(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor profile not found"));
        long studentCount = mentorStudentRepository.findByMentorUserId(userId).size();
        return Map.of(
                "mentor", MentorResponse.summary(mentor),
                "totalStudents", studentCount,
                "totalCourses", mentor.getTaughtCourses().size(),
                "rating", mentor.getRating(),
                "sessions", mentor.getSessionsCount()
        );
    }

    public MentorResponse getMyProfile(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor profile not found"));
        return MentorResponse.from(mentor);
    }

    @Transactional
    public MentorResponse updateProfile(Long userId, MentorProfileUpdateRequest request) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor profile not found"));
        if (request.getBio() != null) mentor.setBio(request.getBio());
        if (request.getLongBio() != null) mentor.setLongBio(request.getLongBio());
        if (request.getLocation() != null) mentor.setLocation(request.getLocation());
        if (request.getAvailable() != null) mentor.setAvailable(request.getAvailable());
        if (request.getSpecialties() != null) mentor.setSpecialties(request.getSpecialties());
        mentorRepository.save(mentor);
        eventProducer.publishProfileUpdated(mentor.getSlug(), mentor.getId());
        return MentorResponse.from(mentor);
    }

    public List<MentorStudent> getStudents(Long userId) {
        return mentorStudentRepository.findByMentorUserId(userId);
    }

    public MentorStudent getStudent(Long userId, Long studentId) {
        return mentorStudentRepository.findByMentorUserIdAndStudentId(userId, studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    public Map<String, Integer> getNotificationsCount() {
        return Map.of("unreadCount", 3);
    }
}
