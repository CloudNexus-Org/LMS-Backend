# Eureka Server

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **service registry** hai. Saari microservices yahan apna naam aur address register karti hain. Jab koi service dusri service ko call karna chahti hai, toh Eureka batata hai ki woh service kahan chal rahi hai.

**Simple words mein:** Phone book jaisa — har service ka number yahan save hota hai.

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `eureka-server` |
| Port | `8761` |
| Base URL | `http://localhost:8761` |
| Dashboard | `http://localhost:8761` (Eureka UI) |
| Total APIs | **0** (yeh business API nahi deti) |
| Database | None |
| Kafka | Not used |

---

## Connected Services (Sab Isse Connected Hain)

Yeh service **sabhi 15 microservices** se connected hai:

- api-gateway
- auth-service
- user-service
- catalog-service
- mentor-service
- content-service
- enrollment-service
- learning-service
- assessment-service
- payment-service
- certificate-service
- review-service
- notification-service
- analytics-service
- admin-service
- media-service

Har service startup par Eureka mein register hoti hai aur heartbeat bhejti rehti hai.

---

## Kya Functionality Add Karni Hai

- [ ] Netflix Eureka Server setup (Spring Cloud)
- [ ] Self-preservation mode enable
- [ ] Health check integration
- [ ] Docker containerization
- [ ] Production mein Eureka cluster (minimum 2 nodes)
- [ ] Security: Eureka dashboard ko basic auth se protect karna

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Eureka Dashboard UI |
| GET | `/eureka/apps` | Registered services list (internal) |

> **Note:** Business APIs yahan nahi hain. Sirf service discovery ke liye hai.

---

## Environment Variables

```env
SERVER_PORT=8761
EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
EUREKA_CLIENT_FETCH_REGISTRY=false
```

---

## Startup Order

**Pehli service jo start honi chahiye** — baaki sab iske baad start karo.
