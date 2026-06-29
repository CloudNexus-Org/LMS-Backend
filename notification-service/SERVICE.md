# Notification Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **in-app aur email notifications** handle karegi. Jab bhi koi important event hota hai (payment, enrollment, assignment due, certificate), yahan notification create hoti hai aur user ko dikhti hai.

**Frontend pages:** `/student/notifications`, `/mentor/notifications`, `/admin/notifications`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `notification-service` |
| Port | `8092` |
| Base URL | `http://localhost:8080/api/notifications` |
| Direct URL | `http://localhost:8092` |
| Total APIs | **8** |
| Database | `lms_notifications` (PostgreSQL) |
| Kafka Topics (Consume) | `user.registered`, `payment.success`, `enrollment.created`, `assignment.submitted`, `assignment.graded`, `certificate.issued`, `course.approved`, `course.rejected`, `otp.sent` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **auth-service** | Kafka consume `otp.sent` | OTP email bhejna |
| **payment-service** | Kafka consume `payment.success` | Payment receipt |
| **enrollment-service** | Kafka consume `enrollment.created` | Welcome to course |
| **assessment-service** | Kafka consume assignment events | Due date, graded alerts |
| **certificate-service** | Kafka consume `certificate.issued` | Congrats notification |
| **admin-service** | Kafka consume `course.approved/rejected` | Mentor ko status update |
| **user-service** | REST | Notification preferences |

**Email Provider:** SendGrid / AWS SES

---

## Kya Functionality Add Karni Hai

- [ ] In-app notification inbox (student, mentor, admin)
- [ ] Mark as read / mark all read
- [ ] Unread count badge
- [ ] Email notifications (configurable per user)
- [ ] Notification preferences (email on/off per type)
- [ ] Kafka consumer for all platform events
- [ ] Push notifications (future — Firebase)
- [ ] Admin broadcast notification
- [ ] Notification templates (HTML email)
- [ ] Delete old notifications (30 day retention)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `notifications` | id, user_id, type, title, message, link, is_read, created_at |
| `notification_preferences` | user_id, email_enrollment, email_payment, email_assignment, email_certificate, push_enabled |
| `email_queue` | id, to_email, subject, body_html, status, sent_at |

---

## API Endpoints (Total: 8)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/notifications/me` | Meri notifications (paginated) | Any |
| 2 | PATCH | `/api/notifications/{id}/read` | Ek notification read mark | Any |
| 3 | PATCH | `/api/notifications/read-all` | Saari read mark | Any |
| 4 | DELETE | `/api/notifications/{id}` | Notification delete | Any |
| 5 | GET | `/api/notifications/unread-count` | Unread count (badge) | Any |
| 6 | PUT | `/api/notifications/preferences` | Preferences update | Any |
| 7 | GET | `/api/notifications/preferences` | Preferences fetch | Any |
| 8 | POST | `/api/notifications/send` | Admin broadcast / internal | Admin, Internal |

---

## Notification Types

| Type | Trigger | Message Example |
|------|---------|-----------------|
| `WELCOME` | user.registered | "Welcome to Cloud Nexus!" |
| `ENROLLMENT` | enrollment.created | "You're enrolled in Cloud Engineer track" |
| `PAYMENT` | payment.success | "Payment of ₹24,999 successful" |
| `ASSIGNMENT_DUE` | Cron job | "Assignment due tomorrow" |
| `ASSIGNMENT_GRADED` | assignment.graded | "Your assignment scored 85/100" |
| `CERTIFICATE` | certificate.issued | "Congratulations! Certificate earned" |
| `COURSE_APPROVED` | course.approved | "Your course is now live!" |
| `COURSE_REJECTED` | course.rejected | "Course needs revision" |
| `OTP` | otp.sent | "Your OTP is 482910" |
