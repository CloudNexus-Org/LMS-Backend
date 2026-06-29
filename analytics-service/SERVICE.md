# Analytics Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **dashboard metrics, reports aur analytics** handle karegi  mentor analytics, student dashboard stats, admin platform reports. Data Kafka events se aggregate hota hai.

**Frontend pages:** `/student/dashboard`, `/mentor/dashboard`, `/mentor/analytics`, `/admin/dashboard`, `/admin/reports`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `analytics-service` |
| Port | `8093` |
| Base URL | `http://localhost:8080/api/analytics` |
| Direct URL | `http://localhost:8093` |
| Total APIs | **10** |
| Database | `lms_analytics` (PostgreSQL + TimescaleDB optional) |
| Kafka Topics (Consume) | `user.registered`, `payment.success`, `lesson.completed`, `quiz.completed`, `track.completed`, `enrollment.created` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **enrollment-service** | REST | Progress data |
| **payment-service** | REST + Kafka | Revenue data |
| **mentor-service** | REST | Mentor student count |
| **catalog-service** | REST | Course metadata |
| **assessment-service** | Kafka | Quiz performance |
| **admin-service** | REST | Reports for admin dashboard |

---

## Kya Functionality Add Karni Hai

- [ ] Student dashboard: courses in progress, hours learned, streak
- [ ] Mentor dashboard: revenue, students, course performance
- [ ] Mentor analytics: revenue chart, enrollment trends, quiz scores
- [ ] Admin dashboard: total users, revenue, active courses, enrollments
- [ ] Admin reports: enrollment report, revenue report, course performance
- [ ] CSV export for reports
- [ ] Real-time metrics via Kafka event aggregation
- [ ] Date range filters on all analytics
- [ ] Caching for heavy dashboard queries (Redis, 1 min TTL)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `daily_metrics` | date, total_users, new_users, total_revenue, enrollments, completions |
| `mentor_metrics` | mentor_id, date, revenue, new_students, active_students |
| `course_metrics` | course_id, date, views, enrollments, completions, avg_rating |
| `student_activity` | user_id, date, lessons_completed, minutes_learned, quizzes_taken |

---

## API Endpoints (Total: 10)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/analytics/mentor/dashboard` | Mentor dashboard summary | Mentor |
| 2 | GET | `/api/analytics/mentor/revenue` | Revenue chart data | Mentor |
| 3 | GET | `/api/analytics/mentor/students` | Student analytics | Mentor |
| 4 | GET | `/api/analytics/mentor/courses/{courseId}` | Single course analytics | Mentor |
| 5 | GET | `/api/analytics/admin/dashboard` | Admin platform dashboard | Admin |
| 6 | GET | `/api/analytics/admin/reports/enrollments` | Enrollment report | Admin |
| 7 | GET | `/api/analytics/admin/reports/revenue` | Revenue report | Admin |
| 8 | GET | `/api/analytics/admin/reports/courses` | Course performance report | Admin |
| 9 | GET | `/api/analytics/admin/export` | CSV export `?type=enrollments&from=&to=` | Admin |
| 10 | GET | `/api/analytics/student/dashboard` | Student dashboard stats | Student |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `StudentDashboardPage.jsx` | GET `/api/analytics/student/dashboard` |
| `MentorDashboardPage.jsx` | GET `/api/analytics/mentor/dashboard` |
| `AnalyticsPage.jsx` | GET `/api/analytics/mentor/revenue`, `/students` |
| `AdminDashboardPage.jsx` | GET `/api/analytics/admin/dashboard` |
| `AdminReportsPage.jsx` | GET `/api/analytics/admin/reports/*` |
| `src/data/mentorDashboard.js` | Replace mock with real API |
| `src/data/stats.js` | GET `/api/analytics/admin/dashboard` |
