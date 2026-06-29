# Learning Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **learning experience** ke extra features handle karegi — notes, bookmarks, Q&A, learning session resume. Lesson player ke tabs (Notes, Q&A) ka data yahan se aayega.

**Frontend pages:** `/student/notes`, `/learn/*` (Notes tab, Q&A tab), `learningSession.js`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `learning-service` |
| Port | `8087` |
| Base URL | `http://localhost:8080/api/learning` |
| Direct URL | `http://localhost:8087` |
| Total APIs | **12** |
| Database | `lms_learning` (PostgreSQL) |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **enrollment-service** | REST | Enrollment check before access |
| **content-service** | REST | Lesson title/metadata for notes |
| **user-service** | REST | User info for Q&A author |

---

## Kya Functionality Add Karni Hai

- [ ] Personal notes per lesson (rich text)
- [ ] Bookmarks — lesson ko bookmark karo, baad mein access
- [ ] Learning session save — last watched lesson resume
- [ ] Q&A per lesson — students questions puch sakte hain
- [ ] Q&A answers (mentor reply — future)
- [ ] Notes search across all courses
- [ ] Export notes as PDF
- [ ] Bookmark folders/tags

---

## Database Tables

| Table | Columns |
|-------|---------|
| `notes` | id, user_id, lesson_id, track_id, content, created_at, updated_at |
| `bookmarks` | id, user_id, lesson_id, track_id, title, created_at |
| `learning_sessions` | id, user_id, track_id, last_lesson_id, last_position_sec, updated_at |
| `lesson_qa` | id, lesson_id, user_id, question, answer, answered_by, created_at |

---

## API Endpoints (Total: 12)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/learning/sessions/resume` | Last learning session (resume URL) | Student |
| 2 | POST | `/api/learning/sessions` | New session save | Student |
| 3 | PUT | `/api/learning/sessions/{sessionId}` | Session update (position) | Student |
| 4 | GET | `/api/learning/notes` | Meri saari notes | Student |
| 5 | POST | `/api/learning/notes` | Nayi note banao | Student |
| 6 | PUT | `/api/learning/notes/{noteId}` | Note update | Student |
| 7 | DELETE | `/api/learning/notes/{noteId}` | Note delete | Student |
| 8 | GET | `/api/learning/bookmarks` | Meri bookmarks | Student |
| 9 | POST | `/api/learning/bookmarks` | Bookmark add | Student |
| 10 | DELETE | `/api/learning/bookmarks/{bookmarkId}` | Bookmark remove | Student |
| 11 | GET | `/api/learning/lessons/{lessonId}/qa` | Lesson Q&A list | Student |
| 12 | POST | `/api/learning/lessons/{lessonId}/qa` | Question post karo | Student |

---

## Frontend Integration

| Frontend | API |
|----------|-----|
| `NotesAndBookmarksPage.jsx` | GET/POST `/api/learning/notes`, `/bookmarks` |
| `NotesPane` in LessonPlayer | POST `/api/learning/notes` |
| `QAPane` in LessonPlayer | GET/POST `/api/learning/lessons/{id}/qa` |
| `learningSession.js` | GET `/api/learning/sessions/resume` |
