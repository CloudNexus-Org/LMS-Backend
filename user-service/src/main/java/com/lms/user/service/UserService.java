package com.lms.user.service;

import com.lms.user.client.AuthProvisioningClient;
import com.lms.user.dto.*;
import com.lms.user.event.UserEventProducer;
import com.lms.user.model.User;
import com.lms.user.model.UserSettings;
import com.lms.user.model.UserStatus;
import com.lms.user.repository.UserRepository;
import com.lms.user.repository.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final DateTimeFormatter JOINED_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US).withZone(ZoneOffset.UTC);

    private static final AtomicLong ID_SEQUENCE = new AtomicLong(100);

    private final UserRepository userRepository;
    private final UserEventProducer eventProducer;
    private final AuthProvisioningClient authProvisioningClient;

    public ProfileResponse getProfile(Long userId, String email) {
        User user = resolveActiveUser(userId, email);
        touchLastActive(user);
        return toProfileResponse(user);
    }

    /** @deprecated use {@link #getProfile(Long, String)} */
    public ProfileResponse getProfile(Long userId) {
        return getProfile(userId, null);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, String email, ProfileUpdateRequest request) {
        User user = resolveActiveUser(userId, email);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername().trim().replaceAll("^@", ""));
        }
        if (request.getProfessionalRole() != null) {
            user.setProfessionalRole(request.getProfessionalRole().trim());
        }
        user.setLastActive(Instant.now());
        userRepository.save(user);
        eventProducer.publishUserUpdated(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        try {
            authProvisioningClient.syncProfileName(user.getEmail(), user.getFullName());
        } catch (Exception ex) {
            // Profile row is already saved; auth display name sync is best-effort
        }
        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateAvatar(Long userId, String email, AvatarUpdateRequest request) {
        User user = resolveActiveUser(userId, email);
        user.setAvatarUrl(request.getAvatarUrl());
        user.setLastActive(Instant.now());
        userRepository.save(user);
        eventProducer.publishUserUpdated(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        return toProfileResponse(user);
    }

    /** @deprecated use {@link #updateAvatar(Long, String, AvatarUpdateRequest)} */
    @Transactional
    public ProfileResponse updateAvatar(Long userId, AvatarUpdateRequest request) {
        return updateAvatar(userId, null, request);
    }

    public SettingsResponse getSettings(Long userId, String email) {
        User user = resolveActiveUser(userId, email);
        return toSettingsResponse(requireSettings(user));
    }

    public SettingsResponse getSettings(Long userId) {
        return getSettings(userId, null);
    }

    @Transactional
    public SettingsResponse updateSettings(Long userId, String email, SettingsUpdateRequest request) {
        User user = resolveActiveUser(userId, email);
        UserSettings settings = requireSettings(user);
        if (request.getTheme() != null) {
            settings.setTheme(request.getTheme());
        }
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getPushNotifications() != null) {
            settings.setPushNotifications(request.getPushNotifications());
        }
        user.setLastActive(Instant.now());
        userRepository.save(user);
        return toSettingsResponse(settings);
    }

    @Transactional
    public SettingsResponse updateSettings(Long userId, SettingsUpdateRequest request) {
        return updateSettings(userId, null, request);
    }

    public PagedResponse<AdminUserResponse> listUsers(String search, String role, String status, int page, int size) {
        Page<User> result = userRepository.findAll(
                UserSpecifications.adminFilter(search, role, status),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedAt")));
        return PagedResponse.<AdminUserResponse>builder()
                .content(result.getContent().stream().map(this::toAdminUserResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public AdminUserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toAdminUserResponse(user);
    }

    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        validateEmailAvailable(request.getEmail(), null);
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        String role = normalizeRole(request.getRole());
        Long userId = nextId();
        User user = buildUser(
                userId,
                request.getEmail(),
                request.getFullName(),
                role,
                request.getPhone(),
                request.getBio(),
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
        attachDefaultSettings(user);
        userRepository.save(user);
        authProvisioningClient.provisionCredential(
                userId,
                user.getEmail(),
                request.getPassword(),
                user.getFullName(),
                role);
        return toAdminUserResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = requireExistingUser(userId);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            validateEmailAvailable(request.getEmail(), userId);
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(normalizeRole(request.getRole()));
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            user.setStatus(UserStatus.valueOf(request.getStatus().trim().toUpperCase()));
        }
        if (request.getProfessionalRole() != null) {
            user.setProfessionalRole(request.getProfessionalRole().trim());
        }
        if (request.getCompany() != null) {
            user.setCompany(request.getCompany().trim());
        }
        if (request.getTrackLabel() != null) {
            user.setTrackLabel(request.getTrackLabel().trim());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation().trim());
        }
        user.setLastActive(Instant.now());
        userRepository.save(user);
        eventProducer.publishUserUpdated(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        return toAdminUserResponse(user);
    }

    @Transactional
    public AdminUserResponse updateStatus(Long userId, StatusUpdateRequest request) {
        User user = requireExistingUser(userId);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot change admin status");
        }
        UserStatus newStatus = UserStatus.valueOf(request.getStatus().trim().toUpperCase());
        user.setStatus(newStatus);
        user.setLastActive(Instant.now());
        userRepository.save(user);
        if (newStatus == UserStatus.BANNED) {
            eventProducer.publishUserBanned(user.getId(), user.getEmail(), "Admin ban");
        }
        return toAdminUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = requireExistingUser(userId);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete admin user");
        }
        user.setStatus(UserStatus.DELETED);
        user.setLastActive(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public MentorListItemResponse createMentor(CreateMentorRequest request) {
        validateMentorRequest(request);
        validateEmailAvailable(request.getEmail(), null);

        Long userId = nextId();
        Instant now = Instant.now();
        User user = User.builder()
                .id(userId)
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName().trim())
                .role("MENTOR")
                .avatarUrl(null)
                .phone(null)
                .bio(trimOrEmpty(request.getBio()))
                .status(UserStatus.ACTIVE)
                .joinedAt(now)
                .lastActive(now)
                .username(request.getUsername().trim())
                .professionalRole(trimOrEmpty(request.getProfessionalRole()))
                .company(trimOrEmpty(request.getCompany()))
                .trackLabel(trimOrEmpty(request.getTrackLabel()))
                .location(trimOrEmpty(request.getLocation()))
                .build();
        attachDefaultSettings(user);
        userRepository.save(user);

        String slug = toSlug(request.getUsername(), request.getFullName());
        Map<String, Object> event = new java.util.HashMap<>();
        event.put("userId", userId);
        event.put("slug", slug);
        event.put("name", user.getFullName());
        event.put("role", user.getProfessionalRole());
        event.put("company", user.getCompany());
        event.put("trackLabel", user.getTrackLabel());
        event.put("bio", user.getBio());
        event.put("location", user.getLocation());
        event.put("avatarUrl", displayAvatar(user));
        event.put("email", user.getEmail());
        event.put("username", user.getUsername());
        authProvisioningClient.provisionMentor(
                userId, user.getEmail(), request.getPassword(), user.getFullName());
        eventProducer.publishMentorCreated(event);
        return toMentorListItem(user);
    }

    public List<MentorListItemResponse> listMentors() {
        return userRepository.findByRoleIgnoreCaseAndStatusNot("MENTOR", UserStatus.DELETED).stream()
                .map(this::toMentorListItem)
                .toList();
    }

    public StudentSummaryResponse getStudentSummary(Long studentId) {
        User user = userRepository.findById(studentId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a student");
        }
        return StudentSummaryResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(displayRole(user.getRole()))
                .avatar(displayAvatar(user))
                .status(displayStatus(user.getStatus()))
                .joined(formatJoined(user.getJoinedAt()))
                .lastActive(formatLastActive(user.getLastActive()))
                .bio(user.getBio())
                .location(user.getLocation())
                .build();
    }

    @Transactional
    public void createProfileFromRegistration(Long userId, String email, String role, String fullName) {
        if (email != null && !email.isBlank()) {
            var existingByEmail = userRepository.findByEmailIgnoreCase(email.trim());
            if (existingByEmail.isPresent()) {
                User existing = existingByEmail.get();
                if (fullName != null && !fullName.isBlank()) {
                    existing.setFullName(fullName.trim());
                }
                if (role != null && !role.isBlank()) {
                    existing.setRole(normalizeRole(role));
                }
                existing.setLastActive(Instant.now());
                userRepository.save(existing);
                return;
            }
        }
        if (userRepository.existsById(userId)) {
            return;
        }
        Instant now = Instant.now();
        User user = buildUser(
                userId,
                email,
                fullName == null || fullName.isBlank() ? email.split("@")[0] : fullName,
                normalizeRole(role),
                null,
                null,
                UserStatus.ACTIVE,
                now,
                now
        );
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            String local = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9._-]", "");
            user.setUsername(local.isBlank() ? "user" + userId : local);
        }
        attachDefaultSettings(user);
        userRepository.save(user);
    }

    public void requireAdmin(String roleHeader) {
        if (roleHeader == null || !"ADMIN".equalsIgnoreCase(roleHeader.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    public void requireMentorOrAdmin(String roleHeader) {
        if (roleHeader == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        String role = roleHeader.trim().toUpperCase();
        if (!"ADMIN".equals(role) && !"MENTOR".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mentor or admin access required");
        }
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ensureActive(user);
    }

    /**
     * Prefer email when present — auth-service and user-service IDs can drift
     * (e.g. admin credential id=1 while users.admin row is id=4).
     */
    private User resolveActiveUser(Long userId, String email) {
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmailIgnoreCase(email.trim())
                    .map(this::ensureActive)
                    .orElseGet(() -> requireActiveUser(userId));
        }
        return requireActiveUser(userId);
    }

    private User ensureActive(User user) {
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is banned");
        }
        return user;
    }

    private User requireExistingUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> u.getStatus() != UserStatus.DELETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void validateEmailAvailable(String email, Long excludeId) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        boolean exists = excludeId == null
                ? userRepository.existsByEmailIgnoreCase(email.trim())
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
    }

    private void validateMentorRequest(CreateMentorRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
    }

    private User buildUser(Long id, String email, String fullName, String role, String phone, String bio,
                           UserStatus status, Instant joinedAt, Instant lastActive) {
        return User.builder()
                .id(id)
                .email(email.trim().toLowerCase())
                .fullName(fullName.trim())
                .role(role)
                .phone(phone)
                .bio(bio)
                .status(status)
                .joinedAt(joinedAt)
                .lastActive(lastActive)
                .build();
    }

    private void attachDefaultSettings(User user) {
        UserSettings settings = UserSettings.builder()
                .user(user)
                .build();
        user.setSettings(settings);
    }

    private UserSettings requireSettings(User user) {
        if (user.getSettings() == null) {
            attachDefaultSettings(user);
            userRepository.save(user);
        }
        return user.getSettings();
    }

    @Transactional
    protected void touchLastActive(User user) {
        user.setLastActive(Instant.now());
        userRepository.save(user);
    }

    private Long nextId() {
        long max = userRepository.findAll().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0L);
        return Math.max(max + 1, ID_SEQUENCE.incrementAndGet());
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(displayRole(user.getRole()))
                .avatar(displayAvatar(user))
                .phone(user.getPhone())
                .bio(user.getBio())
                .location(user.getLocation())
                .username(user.getUsername())
                .professionalRole(user.getProfessionalRole())
                .status(displayStatus(user.getStatus()))
                .joined(formatJoined(user.getJoinedAt()))
                .lastActive(formatLastActive(user.getLastActive()))
                .settings(toSettingsResponse(requireSettings(user)))
                .build();
    }

    private SettingsResponse toSettingsResponse(UserSettings settings) {
        return SettingsResponse.builder()
                .theme(settings.getTheme())
                .language(settings.getLanguage())
                .emailNotifications(settings.getEmailNotifications())
                .pushNotifications(settings.getPushNotifications())
                .build();
    }

    private AdminUserResponse toAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(displayRole(user.getRole()))
                .joined(formatJoined(user.getJoinedAt()))
                .status(displayStatus(user.getStatus()))
                .courses(defaultCourses(user))
                .lastActive(formatLastActive(user.getLastActive()))
                .avatar(displayAvatar(user))
                .spend(defaultSpend(user))
                .username(user.getUsername())
                .professionalRole(user.getProfessionalRole())
                .company(user.getCompany())
                .trackLabel(user.getTrackLabel())
                .location(user.getLocation())
                .bio(user.getBio())
                .build();
    }

    private MentorListItemResponse toMentorListItem(User user) {
        return MentorListItemResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .email(user.getEmail())
                .role(displayRole(user.getRole()))
                .joined(formatJoined(user.getJoinedAt()))
                .status(displayStatus(user.getStatus()))
                .lastActive(formatLastActive(user.getLastActive()))
                .avatar(displayAvatar(user))
                .username(user.getUsername())
                .professionalRole(user.getProfessionalRole())
                .company(user.getCompany())
                .trackLabel(user.getTrackLabel())
                .location(user.getLocation())
                .bio(user.getBio())
                .build();
    }

    private Integer defaultCourses(User user) {
        // Enrollment counts are populated when enrollment-service integration is wired.
        return null;
    }

    private String defaultSpend(User user) {
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return null;
        }
        return null;
    }

    private String displayRole(String role) {
        if (role == null) {
            return "Student";
        }
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Admin";
            case "MENTOR" -> "Mentor";
            default -> "Student";
        };
    }

    private String displayStatus(UserStatus status) {
        return switch (status) {
            case ACTIVE -> "Active";
            case INACTIVE -> "Inactive";
            case BANNED -> "Banned";
            case DELETED -> "Deleted";
        };
    }

    private String displayAvatar(User user) {
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            return user.getAvatarUrl();
        }
        return initials(user.getFullName());
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        return java.util.Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .limit(2)
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT))
                .reduce("", String::concat);
    }

    private String formatJoined(Instant joinedAt) {
        if (joinedAt == null) {
            return "";
        }
        return JOINED_FORMAT.format(joinedAt);
    }

    private String formatLastActive(Instant lastActive) {
        if (lastActive == null) {
            return "Never";
        }
        Duration duration = Duration.between(lastActive, Instant.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " mins ago";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        long days = duration.toDays();
        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }
        return formatJoined(lastActive);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "STUDENT";
        }
        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> "ADMIN";
            case "MENTOR" -> "MENTOR";
            default -> "STUDENT";
        };
    }

    private String toSlug(String username, String fullName) {
        String base = username != null && !username.isBlank()
                ? username.trim().toLowerCase(Locale.ROOT)
                : fullName.trim().toLowerCase(Locale.ROOT);
        return base.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
