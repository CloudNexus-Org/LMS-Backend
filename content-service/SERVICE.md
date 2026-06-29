# Content Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **course curriculum** manage karegi — modules, lessons, video/reading/quiz/project content, resources, transcripts. Mentor yahan course upload karta hai (4-step wizard: Info → Curriculum → Pricing → Publish).

**Frontend pages:** `/mentor/upload`, `/mentor/lessons`, `/learn/:trackId/:lessonId` (lesson data)

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `content-service` |
| Port | `8085` |
| Base URL | `http://localhost:8080/api/content` |
| Direct URL | `http://localhost:8085` |
| Total APIs | **18** |
| Database | `lms_content` (PostgreSQL) |
| Kafka Topics (Produce) | `course.submitted`, `course.published`, `lesson.created` |
| Kafka Topics (Consume) | `course.approved`, `course.rejected` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **media-service** | REST | Video, thumbnail, document upload URLs |
| **catalog-service** | Kafka/REST | Published course catalog mein sync |
| **admin-service** | Kafka produce `course.submitted` | Approval queue mein jaye |
| **assessment-service** | REST | Quiz lessons link |
| **enrollment-service** | REST | Lesson access check |

---

## Kya Functionality Add Karni Hai

- [ ] Course draft create (step 1: title, description, category, level, language)
- [ ] Curriculum builder — modules & lessons add/edit/delete/reorder
- [ ] Lesson types: video, reading, quiz, project
- [ ] Pricing setup (free, paid, premium plans)
- [ ] Submit for admin approval
- [ ] Draft auto-save (localStorage replacement)
- [ ] Lesson resources (PDF, code files) attach
- [ ] Video transcript storage
- [ ] Lesson duration auto-calculate
- [ ] Publish course (after admin approval)
- [ ] Get lessons by track (for lesson player sidebar)
- [ ] Reading content (markdown/HTML)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `courses_content` | id, course_id, mentor_id, status (draft/pending/approved/published), pricing_plan, submitted_at |
| `modules` | id, course_id, title, order_index, description |
| `lessons` | id, module_id, title, type (video/reading/quiz/project), duration_min, order_index, content_url, reading_content, is_preview_free |
| `lesson_resources` | id, lesson_id, title, file_url, file_type |
| `lesson_transcripts` | lesson_id, transcript_text, language |
| `course_drafts` | mentor_id, draft_json, last_saved_at |

---

## API Endpoints (Total: 18)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | POST | `/api/content/courses` | Naya course draft banao | Mentor |
| 2 | PUT | `/api/content/courses/{courseId}` | Course info update | Mentor |
| 3 | GET | `/api/content/courses/{courseId}` | Course detail (mentor view) | Mentor |
| 4 | POST | `/api/content/courses/{courseId}/modules` | Module add karo | Mentor |
| 5 | PUT | `/api/content/courses/{courseId}/modules/{moduleId}` | Module update | Mentor |
| 6 | DELETE | `/api/content/courses/{courseId}/modules/{moduleId}` | Module delete | Mentor |
| 7 | POST | `/api/content/courses/{courseId}/modules/{moduleId}/lessons` | Lesson add | Mentor |
| 8 | PUT | `/api/content/courses/{courseId}/lessons/{lessonId}` | Lesson update | Mentor |
| 9 | DELETE | `/api/content/courses/{courseId}/lessons/{lessonId}` | Lesson delete | Mentor |
| 10 | PUT | `/api/content/courses/{courseId}/curriculum/reorder` | Modules/lessons reorder | Mentor |
| 11 | POST | `/api/content/courses/{courseId}/submit-for-approval` | Admin approval ke liye bhejo | Mentor |
| 12 | GET | `/api/content/courses/drafts` | Mentor ke saare drafts | Mentor |
| 13 | GET | `/api/content/tracks/{trackId}/lessons` | Track ke saare lessons (player sidebar) | Student |
| 14 | GET | `/api/content/lessons/{lessonId}` | Single lesson detail | Student |
| 15 | GET | `/api/content/lessons/{lessonId}/resources` | Lesson resources (PDFs, files) | Student |
| 16 | GET | `/api/content/lessons/{lessonId}/transcript` | Video transcript | Student |
| 17 | PATCH | `/api/content/courses/{courseId}/pricing` | Pricing update | Mentor |
| 18 | POST | `/api/content/courses/{courseId}/publish` | Course publish (approved only) | Mentor |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `UploadCoursePage.jsx` (4 steps) | POST/PUT `/api/content/courses/*` |
| `ManageLessonsPage.jsx` | GET/PUT `/api/content/courses/{id}/modules/*` |
| `LessonPlayerPage.jsx` sidebar | GET `/api/content/tracks/{trackId}/lessons` |
| `LessonPlayerPage.jsx` content | GET `/api/content/lessons/{lessonId}` |
