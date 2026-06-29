# Mentor Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **mentor profiles** manage karegi — public mentor pages, mentor bio, experience, specialties, aur mentor dashboard ke liye basic data. Jab admin naya mentor add karta hai, yahan mentor ka professional profile banta hai.

**Frontend pages:** `/mentors`, `/mentors/:slug`, `/mentor/dashboard`, `/mentor/students`, `/mentor/profile`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `mentor-service` |
| Port | `8084` |
| Base URL | `http://localhost:8080/api/mentors` |
| Direct URL | `http://localhost:8084` |
| Total APIs | **10** |
| Database | `lms_mentors` (PostgreSQL) |
| Kafka Topics (Consume) | `mentor.created`, `user.updated` |
| Kafka Topics (Produce) | `mentor.profile-updated` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **user-service** | Kafka consume `mentor.created` | Admin mentor add → profile create |
| **catalog-service** | REST | Mentor ki courses list |
| **enrollment-service** | REST | Mentor ke students list |
| **analytics-service** | REST | Dashboard metrics |
| **review-service** | REST | Mentor ki average rating |

---

## Kya Functionality Add Karni Hai

- [ ] Public mentor listing (paginated, filter by track/specialty)
- [ ] Mentor detail page (bio, experience, achievements, taught courses)
- [ ] Mentor self-profile edit
- [ ] Mentor's enrolled students list
- [ ] Individual student progress view (mentor ke liye)
- [ ] Mentor availability status
- [ ] Mentor slug-based URLs (`/mentors/arjan-singh`)
- [ ] Hero mentors for track pages
- [ ] Mentor stats (total learners, sessions, rating)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `mentors` | id, user_id, slug, name, role, company, bio, long_bio, avatar_url, track_label, rating, reviews_count, learners_count, sessions_count, years_exp, location, available |
| `mentor_specialties` | mentor_id, specialty |
| `mentor_achievements` | mentor_id, achievement_text, order_index |
| `mentor_experience` | id, mentor_id, title, org, period, description |
| `mentor_taught_courses` | mentor_id, course_id, title, level, modules, hours |

---

## API Endpoints (Total: 10)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/mentors` | Saare mentors list (public) | No |
| 2 | GET | `/api/mentors/{slug}` | Mentor detail by slug | No |
| 3 | GET | `/api/mentors/{slug}/courses` | Mentor ki courses | No |
| 4 | GET | `/api/mentors/{slug}/reviews` | Mentor reviews summary | No |
| 5 | GET | `/api/mentors/me/dashboard` | Mentor apna dashboard summary | Mentor |
| 6 | GET | `/api/mentors/me/profile` | Apna mentor profile | Mentor |
| 7 | PUT | `/api/mentors/me/profile` | Profile update | Mentor |
| 8 | GET | `/api/mentors/me/students` | Enrolled students list | Mentor |
| 9 | GET | `/api/mentors/me/students/{studentId}` | Ek student ki detail + progress | Mentor |
| 10 | GET | `/api/mentors/me/notifications-count` | Unread count (notification-service se) | Mentor |

---

## Frontend Data Mapping

| Frontend File | API |
|---------------|-----|
| `src/data/mentors.js` | GET `/api/mentors/{slug}` |
| `MentorsListPage.jsx` | GET `/api/mentors` |
| `MentorDetailPage.jsx` | GET `/api/mentors/{slug}` |
| `StudentsPage.jsx` | GET `/api/mentors/me/students` |
