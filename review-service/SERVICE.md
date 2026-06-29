# Review Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **course reviews aur ratings** handle karegi. Student completed/in-progress courses par review de sakta hai. Course detail page par reviews dikhengi. Average rating catalog-service ko update hogi.

**Frontend pages:** `/student/reviews`, `/courses/:slug` (reviews section)

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `review-service` |
| Port | `8091` |
| Base URL | `http://localhost:8080/api/reviews` |
| Direct URL | `http://localhost:8091` |
| Total APIs | **8** |
| Database | `lms_reviews` (PostgreSQL) |
| Kafka Topics (Produce) | `review.created`, `review.updated` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **enrollment-service** | REST | Only enrolled students can review |
| **catalog-service** | Kafka produce `review.created` | Update course average rating |
| **user-service** | REST | Reviewer name/avatar |
| **mentor-service** | REST | Mentor reviews summary |

---

## Kya Functionality Add Karni Hai

- [ ] Submit review (rating 1-5, title, body)
- [ ] Edit/delete own review
- [ ] Course reviews list with pagination
- [ ] Review summary (avg rating, distribution)
- [ ] "Helpful" vote on reviews
- [ ] Only completed or 50%+ progress students can review
- [ ] One review per course per student
- [ ] Mentor view their course reviews
- [ ] Admin moderate inappropriate reviews

---

## Database Tables

| Table | Columns |
|-------|---------|
| `reviews` | id, course_id, user_id, rating, title, body, helpful_count, created_at, updated_at |
| `review_helpful_votes` | review_id, user_id, voted_at |

---

## API Endpoints (Total: 8)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/reviews/courses/{courseId}` | Course reviews list | Public |
| 2 | POST | `/api/reviews/courses/{courseId}` | Review submit | Student |
| 3 | PUT | `/api/reviews/{reviewId}` | Review update | Student |
| 4 | DELETE | `/api/reviews/{reviewId}` | Review delete | Student |
| 5 | POST | `/api/reviews/{reviewId}/helpful` | Mark helpful | Any |
| 6 | GET | `/api/reviews/me` | Meri reviews | Student |
| 7 | GET | `/api/reviews/courses/{courseId}/summary` | Rating summary | Public |
| 8 | GET | `/api/reviews/mentor/me` | Mentor ke courses ki reviews | Mentor |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `src/data/courseReviews.js` | GET/POST `/api/reviews/courses/{id}` |
| `CourseReviewsPage.jsx` | GET `/api/reviews/me`, POST reviews |
| `CourseDetailPage.jsx` | GET `/api/reviews/courses/{id}/summary` |
