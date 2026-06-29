# Catalog Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **public catalog** handle karegi — courses list, course detail, career tracks, explore/browse, search, filters, featured content. Yeh mostly **read-heavy** service hai jo frontend ke public pages ko data deti hai.

**Frontend pages:** `/`, `/courses`, `/courses/:slug`, `/tracks`, `/tracks/:id`, `/explore/:type`, Landing page courses section

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `catalog-service` |
| Port | `8083` |
| Base URL | `http://localhost:8080/api/catalog` |
| Direct URL | `http://localhost:8083` |
| Total APIs | **16** |
| Database | `lms_catalog` (PostgreSQL) |
| Cache | Redis (course list, track list — 5 min TTL) |
| Kafka Topics (Consume) | `course.approved`, `course.published`, `review.created` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **content-service** | REST | Course curriculum metadata sync |
| **mentor-service** | REST | Mentor name/photo for course cards |
| **review-service** | Kafka consume `review.created` | Rating update on course |
| **admin-service** | Kafka consume `course.approved` | Approved courses catalog mein dikhao |
| **payment-service** | REST | Course price for cart |

---

## Kya Functionality Add Karni Hai

- [ ] Courses listing with pagination, sort, filter (difficulty, price, rating)
- [ ] Course detail by slug (outcomes, modules count, mentor info)
- [ ] Career tracks listing & detail (Cloud, AI, Fullstack, DevOps, etc.)
- [ ] Track → linked courses mapping
- [ ] Explore browse by category/type
- [ ] Full-text search (title, description, skills)
- [ ] Featured courses for landing page
- [ ] Public stats (total learners, courses count)
- [ ] FAQ & testimonials (CMS-like static content)
- [ ] Filter facets API (difficulty, duration, price range)
- [ ] Course preview (first lesson free flag)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `courses` | id, slug, title, description, mentor_id, difficulty, duration, price, original_price, rating, review_count, enrolled_count, status, thumbnail_url |
| `tracks` | id, slug, name, tagline, description, price, duration_weeks, level, lead_mentor_id, status |
| `track_courses` | track_id, course_id, order_index |
| `categories` | id, name, slug, icon |
| `course_outcomes` | course_id, outcome_text, order_index |
| `course_skills` | course_id, skill_name |
| `faq` | id, question, answer, order_index |
| `testimonials` | id, name, role, company, quote, avatar_url |

---

## API Endpoints (Total: 16)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/catalog/courses` | Saari courses list (filters, pagination) | No |
| 2 | GET | `/api/catalog/courses/{slug}` | Course detail by slug | No |
| 3 | GET | `/api/catalog/courses/featured` | Featured courses (landing page) | No |
| 4 | GET | `/api/catalog/tracks` | Saare career tracks | No |
| 5 | GET | `/api/catalog/tracks/{id}` | Track detail with courses | No |
| 6 | GET | `/api/catalog/tracks/{id}/courses` | Track ke andar ki courses | No |
| 7 | GET | `/api/catalog/explore/{type}` | Explore by type (cloud, ai, etc.) | No |
| 8 | GET | `/api/catalog/explore/search` | Search query `?q=aws&difficulty=intermediate` | No |
| 9 | GET | `/api/catalog/categories` | Saari categories | No |
| 10 | GET | `/api/catalog/courses/{slug}/preview` | Free preview lesson info | No |
| 11 | GET | `/api/catalog/courses/filters` | Available filter options | No |
| 12 | GET | `/api/catalog/stats/public` | Public platform stats | No |
| 13 | GET | `/api/catalog/faq` | FAQ list | No |
| 14 | GET | `/api/catalog/testimonials` | Testimonials | No |
| 15 | GET | `/api/catalog/how-it-works` | How it works steps | No |
| 16 | GET | `/api/catalog/health` | Health check | No |

---

## Frontend Data Mapping

| Frontend File | API |
|---------------|-----|
| `src/data/courses.js` | GET `/api/catalog/courses` |
| `src/data/tracks.js` | GET `/api/catalog/tracks/{id}` |
| `src/data/faq.js` | GET `/api/catalog/faq` |
| `src/data/testimonials.js` | GET `/api/catalog/testimonials` |
| `src/data/howItWorks.js` | GET `/api/catalog/how-it-works` |
| `CoursesListPage.jsx` | GET `/api/catalog/courses?filters...` |
| `TrackDetailPage.jsx` | GET `/api/catalog/tracks/{id}` |
