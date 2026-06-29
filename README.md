# LMS-Backend (Cloud Nexus)

Production-ready microservices backend for the **LMS Frontend** (`../LMS`).

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | Apache Kafka |
| Database | PostgreSQL (per service) |
| Cache | Redis |
| Auth | JWT + Refresh Token |
| File Storage | S3 / Azure Blob (via media-service) |

## Architecture Overview

```
Frontend (React/Vite)
        │
        ▼
  API Gateway :8080
        │
   Eureka Server :8761
        │
  ┌─────┴──────────────────────────────────────────────┐
  │  auth │ user │ catalog │ mentor │ content │ ...     │
  └─────┬──────────────────────────────────────────────┘
        │
     Kafka (async events)
        │
  PostgreSQL (isolated DB per service)
```

## Services (17 total)

| # | Service | Port | APIs | Description |
|---|---------|------|------|-------------|
| 1 | eureka-server | 8761 | - | Service registry |
| 2 | api-gateway | 8080 | - | Single entry point, routing, JWT filter |
| 3 | auth-service | 8081 | 12 | Login, signup, OTP, JWT |
| 4 | user-service | 8082 | 14 | Profiles, admin user management |
| 5 | catalog-service | 8083 | 16 | Courses, tracks, explore, public catalog |
| 6 | mentor-service | 8084 | 10 | Mentor profiles & mentor dashboard |
| 7 | content-service | 8085 | 18 | Curriculum, modules, lessons |
| 8 | enrollment-service | 8086 | 10 | Enrollments & progress tracking |
| 9 | learning-service | 8087 | 12 | Notes, bookmarks, Q&A, sessions |
| 10 | assessment-service | 8088 | 16 | Quizzes & assignments |
| 11 | payment-service | 8089 | 14 | Cart, wishlist, checkout, payments |
| 12 | certificate-service | 8090 | 6 | Certificate issue & verify |
| 13 | review-service | 8091 | 8 | Course reviews & ratings |
| 14 | notification-service | 8092 | 8 | In-app & email notifications |
| 15 | analytics-service | 8093 | 10 | Mentor & admin analytics |
| 16 | admin-service | 8094 | 12 | Approvals, financials, settings |
| 17 | media-service | 8095 | 8 | File & video uploads |

**Total Business APIs: 164**

## Quick Start

```bash
# 1. Start infrastructure (Kafka, Eureka, Postgres, Redis)
docker compose up -d

# 2. Start Eureka first
cd eureka-server && mvn spring-boot:run

# 3. Start API Gateway
cd api-gateway && mvn spring-boot:run

# 4. Start all microservices (order doesn't matter after Eureka)
./scripts/start-all.sh
```

## Frontend Mapping

See `docs/FRONTEND-ANALYSIS.md` for complete page-to-service mapping.

Each service folder contains a **`SERVICE.md`** with:
- Service goal (kya kaam karegi)
- Connected services
- Full API list with base URLs
- Kafka events
- Database tables
- Functionality checklist

## Base URL Convention

All APIs are accessed through the gateway:

```
http://localhost:8080/api/{service-prefix}/...
```

Example:
```
GET http://localhost:8080/api/catalog/courses
POST http://localhost:8080/api/auth/login
```

## Kafka Topics (shared)

| Topic | Producer | Consumer |
|-------|----------|----------|
| user.registered | auth-service | notification-service, analytics-service |
| enrollment.created | payment-service | enrollment-service, notification-service |
| course.approved | admin-service | catalog-service, notification-service |
| progress.completed | enrollment-service | certificate-service, analytics-service |
| payment.success | payment-service | enrollment-service, notification-service |
| certificate.issued | certificate-service | notification-service |
| assignment.submitted | assessment-service | notification-service |
| quiz.completed | assessment-service | analytics-service |
