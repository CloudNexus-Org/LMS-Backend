# Auth Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **login, signup, password reset, OTP verification** aur **JWT token** manage karegi. Har user (student, mentor, admin) yahan authenticate hoga. Baaki saari services token validate karke user ko pehchanengi.

**Frontend pages:** `/login`, `/signup`, `/forgot-password`, `/verify-otp`

**Simple words mein:** Gatekeeper — kaun allowed hai aur kaun nahi, yeh decide karta hai.

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `auth-service` |
| Port | `8081` |
| Base URL | `http://localhost:8080/api/auth` |
| Direct URL | `http://localhost:8081` |
| Total APIs | **12** |
| Database | `lms_auth` (PostgreSQL) |
| Kafka Topics (Produce) | `user.registered`, `user.password-reset`, `otp.sent` |

---

## Connected Services

| Service | Connection Type | Kyun |
|---------|----------------|------|
| **user-service** | REST (sync) | Signup ke baad user profile create karna |
| **notification-service** | Kafka (async) | OTP email, welcome email bhejna |
| **api-gateway** | Registered in Eureka | Gateway is service ko route karta hai |
| **eureka-server** | Client registration | Service discovery |

---

## Kya Functionality Add Karni Hai

- [ ] User registration (email + password)
- [ ] Login with JWT access token + refresh token
- [ ] OTP generation & verification (6 digit, 5 min expiry)
- [ ] Forgot password flow (email → OTP → reset)
- [ ] Password hashing (BCrypt)
- [ ] Role assignment: STUDENT, MENTOR, ADMIN
- [ ] Refresh token rotation
- [ ] Token blacklist (logout ke liye Redis mein)
- [ ] Rate limit on login (5 failed attempts = 15 min lock)
- [ ] Remember me (longer refresh token)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `auth_credentials` | id, email, password_hash, role, is_active, created_at |
| `refresh_tokens` | id, user_id, token_hash, expires_at, revoked |
| `otp_codes` | id, email, code, purpose, expires_at, used |
| `login_attempts` | id, email, ip, success, attempted_at |

---

## API Endpoints (Total: 12)

| # | Method | Endpoint | Description | Auth Required |
|---|--------|----------|-------------|---------------|
| 1 | POST | `/api/auth/register` | Naya account banao (student default) | No |
| 2 | POST | `/api/auth/login` | Email + password se login, JWT milega | No |
| 3 | POST | `/api/auth/logout` | Token invalidate karo | Yes |
| 4 | POST | `/api/auth/refresh-token` | Naya access token lo refresh token se | No (refresh token) |
| 5 | POST | `/api/auth/forgot-password` | Password reset OTP email bhejo | No |
| 6 | POST | `/api/auth/verify-otp` | OTP verify karo | No |
| 7 | POST | `/api/auth/resend-otp` | OTP dubara bhejo | No |
| 8 | POST | `/api/auth/reset-password` | Naya password set karo (OTP verified) | No |
| 9 | GET | `/api/auth/me` | Current logged-in user ki basic info | Yes |
| 10 | POST | `/api/auth/validate-token` | Gateway/internal — token valid hai ya nahi | Internal |
| 11 | POST | `/api/auth/change-password` | Logged-in user password change | Yes |
| 12 | GET | `/api/auth/health` | Service health check | No |

---

## Kafka Events

| Event | When | Payload |
|-------|------|---------|
| `user.registered` | Signup success | `{ userId, email, role, fullName }` |
| `otp.sent` | OTP generated | `{ email, purpose, expiresAt }` |
| `user.password-reset` | Password changed | `{ userId, email }` |

---

## Frontend Integration

```javascript
// LoginPage.jsx — replace mock login with:
POST http://localhost:8080/api/auth/login
Body: { email, password, rememberMe }
Response: { accessToken, refreshToken, user: { id, email, role, fullName } }
```
