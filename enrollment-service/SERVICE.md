# Enrollment Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **enrollment aur progress tracking** handle karegi. Jab student course/track kharidta hai, yahan enrollment record banta hai. Lesson complete hone par progress update hota hai. "My Courses" aur dashboard progress yahan se aata hai.

**Frontend pages:** `/student/courses`, `/student/dashboard`, `/learn/*` (progress ring), `useCourseProgress` hook

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `enrollment-service` |
| Port | `8086` |
| Base URL | `http://localhost:8080/api/enrollments` |
| Direct URL | `http://localhost:8086` |
| Total APIs | **10** |
| Database | `lms_enrollment` (PostgreSQL) |
| Kafka Topics (Consume) | `enrollment.created`, `payment.success` |
| Kafka Topics (Produce) | `progress.completed`, `track.completed`, `lesson.completed` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **payment-service** | Kafka consume `payment.success` | Payment ke baad auto-enroll |
| **catalog-service** | REST | Course/track metadata |
| **content-service** | REST | Total lessons count for progress % |
| **certificate-service** | Kafka produce `track.completed` | 100% complete → certificate trigger |
| **analytics-service** | Kafka produce progress events | Dashboard metrics |
| **learning-service** | REST | Last learning session resume URL |

---

## Kya Functionality Add Karni Hai

- [ ] Auto-enrollment after successful payment
- [ ] Manual enrollment (free courses)
- [ ] My enrolled courses/tracks list with progress %
- [ ] Per-lesson completion tracking
- [ ] Track-level progress calculation
- [ ] Student dashboard summary (courses in progress, completed count)
- [ ] Enrollment expiry (optional, for subscription tracks)
- [ ] Check if user enrolled before lesson access
- [ ] Resume learning — last accessed lesson
- [ ] Admin: view/manage enrollments

---

## Database Tables

| Table | Columns |
|-------|---------|
| `enrollments` | id, user_id, track_id, course_id, enrolled_at, status, expires_at |
| `lesson_progress` | id, user_id, lesson_id, track_id, completed, completed_at, watch_duration_sec |
| `track_progress` | user_id, track_id, completed_lessons, total_lessons, progress_pct, last_lesson_id, updated_at |

---

## API Endpoints (Total: 10)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | POST | `/api/enrollments` | Enroll karo (free course ya manual) | Student |
| 2 | GET | `/api/enrollments/me` | Meri saari enrollments | Student |
| 3 | GET | `/api/enrollments/me/{trackId}` | Ek track ki enrollment detail | Student |
| 4 | GET | `/api/enrollments/me/courses/{courseId}/progress` | Course progress detail | Student |
| 5 | PUT | `/api/enrollments/progress/lessons/{lessonId}` | Lesson progress update (watch time) | Student |
| 6 | GET | `/api/enrollments/progress/tracks/{trackId}` | Track progress summary | Student |
| 7 | POST | `/api/enrollments/progress/lessons/{lessonId}/complete` | Lesson mark as complete | Student |
| 8 | GET | `/api/enrollments/dashboard/student` | Student dashboard stats | Student |
| 9 | GET | `/api/enrollments/check/{trackId}` | Enrolled hai ya nahi check | Student |
| 10 | DELETE | `/api/enrollments/{enrollmentId}` | Enrollment cancel (admin) | Admin |

---

## Kafka Events

| Event | When | Consumer |
|-------|------|----------|
| `payment.success` | Payment complete | Auto-create enrollment |
| `lesson.completed` | Student completes lesson | analytics-service |
| `track.completed` | 100% track done | certificate-service |
| `progress.completed` | Milestone reached | notification-service |

---

## Frontend Integration

```javascript
// useCourseProgress hook replacement:
GET /api/enrollments/progress/tracks/{trackId}
PUT /api/enrollments/progress/lessons/{lessonId}
POST /api/enrollments/progress/lessons/{lessonId}/complete
```
