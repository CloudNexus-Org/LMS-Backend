# User Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **user profiles, settings, avatar** aur **admin user management** handle karegi. Signup ke baad yahan user ka full profile banta hai. Admin yahan se users ko manage karta hai — ban, activate, mentor add karna.

**Frontend pages:** `/student/profile`, `/student/settings`, `/admin/users`, `/admin/users/add-mentor`, `/admin/profile`, `/mentor/profile`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `user-service` |
| Port | `8082` |
| Base URL | `http://localhost:8080/api/users` |
| Direct URL | `http://localhost:8082` |
| Total APIs | **14** |
| Database | `lms_users` (PostgreSQL) |
| Kafka Topics (Consume) | `user.registered` |
| Kafka Topics (Produce) | `user.updated`, `user.banned`, `mentor.created` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **auth-service** | Kafka consume `user.registered` | Signup ke baad profile auto-create |
| **auth-service** | REST | Admin mentor add karte waqt credentials bhi banate hain |
| **mentor-service** | Kafka produce `mentor.created` | Mentor profile sync |
| **media-service** | REST | Avatar upload |
| **notification-service** | Kafka | Profile update, ban notification |
| **admin-service** | REST (called by admin) | User list for admin dashboard |

---

## Kya Functionality Add Karni Hai

- [ ] User profile CRUD (name, bio, phone, location)
- [ ] Avatar upload (media-service se URL)
- [ ] Notification preferences (email, push)
- [ ] Theme/language preferences
- [ ] Admin: list all users with filters (role, status, search)
- [ ] Admin: ban/unban user
- [ ] Admin: add new mentor (creates user + triggers mentor-service)
- [ ] Admin: edit user details
- [ ] Student profile page data
- [ ] Last active tracking
- [ ] GDPR: account deletion request

---

## Database Tables

| Table | Columns |
|-------|---------|
| `users` | id, email, full_name, role, avatar_url, phone, bio, status, joined_at, last_active |
| `user_settings` | user_id, theme, language, email_notifications, push_notifications |
| `user_addresses` | id, user_id, city, country, pincode |

---

## API Endpoints (Total: 14)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/users/profile` | Apna profile dekho | Any logged-in |
| 2 | PUT | `/api/users/profile` | Profile update karo | Any logged-in |
| 3 | PATCH | `/api/users/profile/avatar` | Avatar URL update | Any logged-in |
| 4 | PUT | `/api/users/profile/settings` | Settings update (theme, notifications) | Any logged-in |
| 5 | GET | `/api/users/profile/settings` | Settings fetch | Any logged-in |
| 6 | GET | `/api/users` | Saare users list (admin) | Admin |
| 7 | GET | `/api/users/{userId}` | Ek user ki detail | Admin |
| 8 | POST | `/api/users` | Naya user create (admin) | Admin |
| 9 | PUT | `/api/users/{userId}` | User update | Admin |
| 10 | PATCH | `/api/users/{userId}/status` | Ban/Unban/Activate | Admin |
| 11 | DELETE | `/api/users/{userId}` | User delete (soft) | Admin |
| 12 | POST | `/api/users/mentors` | Naya mentor add karo | Admin |
| 13 | GET | `/api/users/mentors` | Mentors list (admin view) | Admin |
| 14 | GET | `/api/users/students/{studentId}/summary` | Student summary (mentor/admin) | Mentor, Admin |

---

## Kafka Events

| Event | Direction | When |
|-------|-----------|------|
| `user.registered` | Consume | auth-service se — profile create |
| `user.updated` | Produce | Profile change |
| `user.banned` | Produce | Admin ban kare |
| `mentor.created` | Produce | Admin mentor add kare |
