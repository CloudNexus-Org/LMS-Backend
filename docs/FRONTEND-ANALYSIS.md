# LMS Frontend Analysis → Backend Mapping

> Source: `/Users/raunakdhanotiya/Desktop/LMS` (React 19 + Vite + Zustand)

## Current Frontend State

- **No real backend** — sab data mock/localStorage/sessionStorage se aa raha hai
- **3 user roles:** `student`, `mentor`, `admin`
- **Brand:** Cloud Nexus (lmsland)

---

## Frontend Pages → Backend Services

### Public Pages (bina login ke)

| Frontend Route | Page | Backend Service(s) |
|----------------|------|-------------------|
| `/` | LandingPage | catalog-service, analytics-service (stats) |
| `/demo` | DemoPage | catalog-service |
| `/mentors` | MentorsListPage | mentor-service |
| `/mentors/:slug` | MentorDetailPage | mentor-service, catalog-service |
| `/tracks` | TracksListPage | catalog-service |
| `/tracks/:id` | TrackDetailPage | catalog-service, mentor-service |
| `/courses` | CoursesListPage | catalog-service |
| `/courses/:slug` | CourseDetailPage | catalog-service, review-service |
| `/explore/:type` | ExploreBrowsePage | catalog-service |
| `/cart` | CartPage | payment-service |

### Auth Pages

| Frontend Route | Page | Backend Service(s) |
|----------------|------|-------------------|
| `/login` | LoginPage | auth-service |
| `/signup` | SignupPage | auth-service, user-service |
| `/forgot-password` | ForgotPasswordPage | auth-service |
| `/verify-otp` | OtpVerificationPage | auth-service |

### Learning (Lesson Player)

| Frontend Route | Page | Backend Service(s) |
|----------------|------|-------------------|
| `/learn/:trackId` | LessonPlayerPage | content-service, enrollment-service, learning-service, assessment-service, media-service |
| `/learn/:trackId/:lessonId` | LessonPlayerPage | same as above |

**Lesson Player Features:**
- Video playback → media-service
- Reading content → content-service
- Quiz pane → assessment-service
- Notes & bookmarks → learning-service
- Transcript & resources → content-service
- Q&A tab → learning-service
- Progress ring → enrollment-service

### Student Dashboard (`/student/*`)

| Route | Page | Services |
|-------|------|----------|
| `/student/dashboard` | StudentDashboardPage | enrollment-service, analytics-service |
| `/student/courses` | MyCoursesPage | enrollment-service, catalog-service |
| `/student/catalog` | StudentBrowseCoursesPage | catalog-service |
| `/student/cart` | StudentCartPage | payment-service |
| `/student/certificates` | CertificatesPage | certificate-service |
| `/student/certificates/:id` | CertificateDetailPage | certificate-service |
| `/student/wishlist` | StudentWishlistPage | payment-service |
| `/student/reviews` | CourseReviewsPage | review-service, enrollment-service |
| `/student/quizzes` | PracticeQuizzesPage | assessment-service |
| `/student/notes` | NotesAndBookmarksPage | learning-service |
| `/student/settings` | ProfileSettingsPage | user-service |
| `/student/payment` | CoursePaymentPage | payment-service, catalog-service |
| `/student/assignments` | PendingAssignmentsPage | assessment-service |
| `/student/notifications` | NotificationsPage | notification-service |
| `/student/profile` | ProfilePage | user-service |
| `/student/quiz` | QuizResultsPage | assessment-service |

### Mentor Dashboard (`/mentor/*`)

| Route | Page | Services |
|-------|------|----------|
| `/mentor/dashboard` | MentorDashboardPage | analytics-service, mentor-service |
| `/mentor/upload` | UploadCoursePage | content-service, media-service |
| `/mentor/lessons` | ManageLessonsPage | content-service |
| `/mentor/quizzes` | ManageQuizzesPage | assessment-service |
| `/mentor/analytics` | AnalyticsPage | analytics-service |
| `/mentor/students` | StudentsPage | mentor-service, enrollment-service |
| `/mentor/notifications` | MentorNotificationsPage | notification-service |
| `/mentor/profile` | ProfileSettingsPage | user-service, mentor-service |

### Admin Dashboard (`/admin/*`)

| Route | Page | Services |
|-------|------|----------|
| `/admin/dashboard` | AdminDashboardPage | analytics-service, admin-service |
| `/admin/users` | UserManagementPage | user-service |
| `/admin/users/add-mentor` | AddMentorPage | user-service, auth-service |
| `/admin/approvals` | CourseApprovalsPage | admin-service, content-service |
| `/admin/revenue` | FinancialsPage | admin-service, payment-service |
| `/admin/reports` | AdminReportsPage | analytics-service, admin-service |
| `/admin/settings` | SystemSettingsPage | admin-service |
| `/admin/notifications` | AdminNotificationsPage | notification-service |
| `/admin/profile` | AdminProfilePage | user-service |

---

## Frontend Data Models (Entities)

| Entity | Source File | Owner Service |
|--------|-------------|---------------|
| User (student/mentor/admin) | useAuthStore, adminUsers.js | user-service |
| Course | courses.js | catalog-service + content-service |
| Track (career path) | tracks.js | catalog-service |
| Mentor | mentors.js | mentor-service |
| Lesson | tracks.js (getLessonsByTrack) | content-service |
| Quiz | quizzes.js | assessment-service |
| Assignment | assignments.js | assessment-service |
| Certificate | certificates.js | certificate-service |
| Review | courseReviews.js | review-service |
| Cart | useCartStore.js | payment-service |
| Wishlist | useWishlistStore.js | payment-service |
| Progress | useCourseProgress hook | enrollment-service |
| Notes/Bookmarks | NotesAndBookmarksPage | learning-service |
| Notification | NotificationsPage | notification-service |
| Transaction | FinancialsPage | admin-service + payment-service |
| Course Approval | CourseApprovalsPage | admin-service |

---

## Frontend Stores → API Replacement

| Zustand Store | Replace With |
|---------------|-------------|
| useAuthStore | auth-service (JWT in httpOnly cookie or Authorization header) |
| useCartStore | payment-service `/api/payments/cart` |
| useWishlistStore | payment-service `/api/payments/wishlist` |
| sessionStorage (reviews, quizzes, admin users) | respective microservice APIs |
