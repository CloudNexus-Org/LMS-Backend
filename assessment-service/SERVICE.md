# Assessment Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **quizzes aur assignments** dono handle karegi. Mentor quizzes banata hai, student attempt karta hai. Assignments submit karte hain, mentor grade karta hai.

**Frontend pages:** `/student/quizzes`, `/student/quiz`, `/student/assignments`, `/mentor/quizzes`, Lesson player Quiz tab

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `assessment-service` |
| Port | `8088` |
| Base URL | `http://localhost:8080/api/assessment` |
| Direct URL | `http://localhost:8088` |
| Total APIs | **16** |
| Database | `lms_assessment` (PostgreSQL) |
| Kafka Topics (Produce) | `quiz.completed`, `assignment.submitted`, `assignment.graded` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **content-service** | REST | Quiz lessons link to curriculum |
| **enrollment-service** | REST | Only enrolled students can attempt |
| **notification-service** | Kafka | Assignment due, graded notifications |
| **analytics-service** | Kafka consume `quiz.completed` | Performance metrics |
| **media-service** | REST | Assignment file uploads |

---

## Kya Functionality Add Karni Hai

- [ ] Mentor: create/edit/delete quizzes with MCQ questions
- [ ] Quiz per lesson or standalone practice quiz
- [ ] Student: attempt quiz, get score & explanations
- [ ] Quiz attempt history & results page
- [ ] Timer for timed quizzes
- [ ] Assignments: mentor create with due date, points
- [ ] Student: view pending assignments, submit (file/link)
- [ ] Mentor: grade submissions, give feedback
- [ ] Auto-grade MCQ quizzes
- [ ] Passing score threshold
- [ ] Retry limit for quizzes

---

## Database Tables

| Table | Columns |
|-------|---------|
| `quizzes` | id, mentor_id, lesson_id, track_id, title, passing_score, time_limit_min, created_at |
| `quiz_questions` | id, quiz_id, question, options (JSON), correct_index, explanation, topic |
| `quiz_attempts` | id, quiz_id, user_id, score, total, answers (JSON), completed_at |
| `assignments` | id, mentor_id, course_id, track_id, title, description, instructions, due_date, points, priority |
| `assignment_submissions` | id, assignment_id, user_id, submission_url, submitted_at, grade, feedback, graded_at |

---

## API Endpoints (Total: 16)

### Quizzes (9 APIs)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/assessment/quizzes/tracks/{trackId}` | Track ke quizzes | Student |
| 2 | GET | `/api/assessment/quizzes/{quizId}` | Quiz detail (questions) | Student |
| 3 | POST | `/api/assessment/quizzes/{quizId}/attempts` | Quiz attempt submit | Student |
| 4 | GET | `/api/assessment/quizzes/attempts/me` | Meri saari attempts | Student |
| 5 | GET | `/api/assessment/quizzes/attempts/{attemptId}` | Attempt result detail | Student |
| 6 | POST | `/api/assessment/quizzes` | Naya quiz banao | Mentor |
| 7 | PUT | `/api/assessment/quizzes/{quizId}` | Quiz update | Mentor |
| 8 | DELETE | `/api/assessment/quizzes/{quizId}` | Quiz delete | Mentor |
| 9 | GET | `/api/assessment/quizzes/mentor/me` | Mentor ke saare quizzes | Mentor |

### Assignments (7 APIs)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 10 | GET | `/api/assessment/assignments/me/pending` | Pending assignments | Student |
| 11 | GET | `/api/assessment/assignments/{assignmentId}` | Assignment detail | Student |
| 12 | POST | `/api/assessment/assignments/{assignmentId}/submissions` | Submit assignment | Student |
| 13 | GET | `/api/assessment/assignments/submissions/me` | Meri submissions | Student |
| 14 | POST | `/api/assessment/assignments` | Assignment create | Mentor |
| 15 | PUT | `/api/assessment/assignments/{assignmentId}/grade` | Grade submission | Mentor |
| 16 | GET | `/api/assessment/assignments/mentor/me` | Mentor ke assignments | Mentor |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `src/data/quizzes.js` | GET `/api/assessment/quizzes/tracks/{trackId}` |
| `QuizPane.jsx` | POST `/api/assessment/quizzes/{id}/attempts` |
| `PracticeQuizzesPage.jsx` | GET `/api/assessment/quizzes/tracks/{trackId}` |
| `QuizResultsPage.jsx` | GET `/api/assessment/quizzes/attempts/{id}` |
| `PendingAssignmentsPage.jsx` | GET `/api/assessment/assignments/me/pending` |
| `ManageQuizzesPage.jsx` | CRUD `/api/assessment/quizzes` |
