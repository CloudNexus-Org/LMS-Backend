# LMS Backend Architecture

## System Diagram

```mermaid
flowchart TB
    subgraph Client
        FE[LMS Frontend React/Vite :5173]
    end

    subgraph Gateway Layer
        GW[API Gateway :8080]
        EU[Eureka Server :8761]
    end

    subgraph Core Services
        AUTH[auth-service :8081]
        USER[user-service :8082]
        CAT[catalog-service :8083]
        MENTOR[mentor-service :8084]
        CONTENT[content-service :8085]
    end

    subgraph Learning Services
        ENROLL[enrollment-service :8086]
        LEARN[learning-service :8087]
        ASSESS[assessment-service :8088]
        CERT[certificate-service :8090]
    end

    subgraph Commerce Services
        PAY[payment-service :8089]
        REV[review-service :8091]
    end

    subgraph Platform Services
        NOTIF[notification-service :8092]
        ANALYTICS[analytics-service :8093]
        ADMIN[admin-service :8094]
        MEDIA[media-service :8095]
    end

    subgraph Infrastructure
        KAFKA[Apache Kafka :9092]
        PG[(PostgreSQL :5432)]
        REDIS[(Redis :6379)]
        S3[(S3 / Blob Storage)]
    end

    FE --> GW
    GW --> EU
    GW --> AUTH & USER & CAT & MENTOR & CONTENT
    GW --> ENROLL & LEARN & ASSESS & PAY & CERT & REV
    GW --> NOTIF & ANALYTICS & ADMIN & MEDIA

    AUTH --> KAFKA
    PAY --> KAFKA
    ENROLL --> KAFKA
    ASSESS --> KAFKA
    ADMIN --> KAFKA
    CERT --> KAFKA
    KAFKA --> NOTIF & ANALYTICS & ENROLL & CAT

    AUTH & USER & CAT & MENTOR & CONTENT --> PG
    ENROLL & LEARN & ASSESS & PAY & CERT & REV --> PG
    NOTIF & ANALYTICS & ADMIN & MEDIA --> PG
    MEDIA --> S3
    GW --> REDIS
```

## Service Communication Patterns

### Synchronous (REST via Eureka)
- Gateway → Any service (HTTP routing)
- content-service → media-service (file upload)
- payment-service → catalog-service (price fetch)
- admin-service → content-service (approval queue)

### Asynchronous (Kafka Events)
- auth-service → notification-service (`user.registered`)
- payment-service → enrollment-service (`payment.success`)
- enrollment-service → certificate-service (`track.completed`)
- admin-service → catalog-service (`course.approved`)

## Database Strategy

**Database-per-service** pattern:
- Har service ka apna PostgreSQL database
- Cross-service data access sirf API ya Kafka se
- Shared PostgreSQL instance, separate schemas/databases

## Security

1. JWT issued by auth-service
2. API Gateway validates token on every request
3. Role-based access at gateway level
4. Service-to-service: internal network only
5. Secrets via environment variables / Vault

## Deployment Order

```
1. PostgreSQL, Redis, Kafka, Zookeeper
2. eureka-server
3. api-gateway
4. auth-service, user-service (core)
5. catalog-service, mentor-service, content-service
6. enrollment, learning, assessment, payment
7. certificate, review, notification, analytics, admin, media
```

## Total API Summary

| Service | APIs |
|---------|------|
| auth-service | 12 |
| user-service | 14 |
| catalog-service | 16 |
| mentor-service | 10 |
| content-service | 18 |
| enrollment-service | 10 |
| learning-service | 12 |
| assessment-service | 16 |
| payment-service | 14 |
| certificate-service | 6 |
| review-service | 8 |
| notification-service | 8 |
| analytics-service | 10 |
| admin-service | 12 |
| media-service | 8 |
| **TOTAL** | **164** |

> Gateway + Eureka have 2 infrastructure endpoints each (health/actuator)
