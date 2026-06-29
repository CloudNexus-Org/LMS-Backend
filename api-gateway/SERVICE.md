# API Gateway Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh **single entry point** hai poori backend ka. Frontend sirf is gateway se baat karega (`localhost:8080`). Gateway request ko sahi microservice tak route karega, JWT token validate karega, rate limiting lagaega, aur CORS handle karega.

**Simple words mein:** Reception desk — sab visitors yahan aate hain, phir andar sahi department mein bheje jaate hain.

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `api-gateway` |
| Port | `8080` |
| Base URL | `http://localhost:8080` |
| Total APIs | **0 business APIs** (routing + filters) |
| Database | None (Redis for rate limit) |
| Kafka | Optional (request logging) |

---

## Connected Services

| Service | Gateway Route Prefix | Internal Port |
|---------|---------------------|---------------|
| auth-service | `/api/auth/**` | 8081 |
| user-service | `/api/users/**` | 8082 |
| catalog-service | `/api/catalog/**` | 8083 |
| mentor-service | `/api/mentors/**` | 8084 |
| content-service | `/api/content/**` | 8085 |
| enrollment-service | `/api/enrollments/**` | 8086 |
| learning-service | `/api/learning/**` | 8087 |
| assessment-service | `/api/assessment/**` | 8088 |
| payment-service | `/api/payments/**` | 8089 |
| certificate-service | `/api/certificates/**` | 8090 |
| review-service | `/api/reviews/**` | 8091 |
| notification-service | `/api/notifications/**` | 8092 |
| analytics-service | `/api/analytics/**` | 8093 |
| admin-service | `/api/admin/**` | 8094 |
| media-service | `/api/media/**` | 8095 |

**Discovery:** eureka-server se sab services ka address milta hai.

**Auth validation:** auth-service se JWT verify hota hai.

---

## Kya Functionality Add Karni Hai

- [ ] Spring Cloud Gateway setup
- [ ] Eureka client integration (dynamic routing)
- [ ] JWT authentication filter (public routes exclude: login, signup, catalog public)
- [ ] CORS configuration (frontend: `http://localhost:5173`)
- [ ] Rate limiting (Redis based — 100 req/min per IP)
- [ ] Request/Response logging
- [ ] Circuit breaker (Resilience4j) har service ke liye
- [ ] Global exception handler
- [ ] API versioning header support
- [ ] Health check endpoint: `GET /actuator/health`

---

## Public Routes (Bina Token Ke)

```
POST /api/auth/login
POST /api/auth/register
POST /api/auth/forgot-password
POST /api/auth/verify-otp
GET  /api/catalog/courses
GET  /api/catalog/courses/{slug}
GET  /api/catalog/tracks
GET  /api/catalog/tracks/{id}
GET  /api/mentors
GET  /api/mentors/{slug}
GET  /api/certificates/verify/{code}
GET  /api/catalog/explore/**
```

---

## Role-Based Route Protection

| Role | Allowed Prefixes |
|------|-----------------|
| student | `/api/enrollments/**`, `/api/learning/**`, `/api/payments/**`, `/api/reviews/**` |
| mentor | `/api/content/**`, `/api/assessment/**` (own), `/api/analytics/mentor/**` |
| admin | `/api/admin/**`, `/api/users/**` (full CRUD) |

---

## Gateway Endpoints (Sirf Gateway Ke Apne)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Gateway health check |
| GET | `/actuator/gateway/routes` | Active routes list |

**Total Gateway-specific endpoints: 2**
