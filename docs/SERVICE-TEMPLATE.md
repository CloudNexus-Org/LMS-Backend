# Service Template

Har microservice ka same base structure follow karna hai. `auth-service` ko reference template maan sakte ho.

## Standard Folder Structure (Har Service Mein)

```
{service-name}/
├── SERVICE.md              ← Service documentation (mandatory)
├── pom.xml                 ← Maven dependencies
├── Dockerfile              ← Container build
└── src/
    ├── main/
    │   ├── java/com/lms/{service_name}/
    │   │   ├── {Service}Application.java
    │   │   ├── config/     ← Kafka, Security, Eureka config
    │   │   ├── controller/ ← REST API endpoints
    │   │   ├── service/    ← Business logic
    │   │   ├── repository/ ← JPA repositories
    │   │   ├── model/      ← Entity classes
    │   │   ├── dto/        ← Request/Response DTOs
    │   │   └── event/      ← Kafka producers/consumers
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/lms/
```

## Maven Dependencies (Standard)

```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-cloud-starter-netflix-eureka-client
- spring-kafka
- postgresql
- spring-boot-starter-actuator
- lombok
```

## Port Assignment

| Service | Port | Database |
|---------|------|----------|
| eureka-server | 8761 | - |
| api-gateway | 8080 | - |
| auth-service | 8081 | lms_auth |
| user-service | 8082 | lms_users |
| catalog-service | 8083 | lms_catalog |
| mentor-service | 8084 | lms_mentors |
| content-service | 8085 | lms_content |
| enrollment-service | 8086 | lms_enrollment |
| learning-service | 8087 | lms_learning |
| assessment-service | 8088 | lms_assessment |
| payment-service | 8089 | lms_payments |
| certificate-service | 8090 | lms_certificates |
| review-service | 8091 | lms_reviews |
| notification-service | 8092 | lms_notifications |
| analytics-service | 8093 | lms_analytics |
| admin-service | 8094 | lms_admin |
| media-service | 8095 | lms_media |

## Next Steps (Implementation Order)

1. `eureka-server` + `api-gateway` setup
2. `auth-service` + `user-service` (login/signup flow)
3. `catalog-service` + `mentor-service` (public pages)
4. `content-service` + `media-service` (course upload)
5. `payment-service` + `enrollment-service` (buy & enroll)
6. `learning-service` + `assessment-service` (lesson player)
7. `certificate-service` + `review-service`
8. `notification-service` + `analytics-service` + `admin-service`
