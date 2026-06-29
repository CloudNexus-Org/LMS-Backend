# Payment Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **cart, wishlist, checkout aur payment** handle karegi. Student track/course cart mein daalta hai, payment karta hai, order record banta hai. Payment success par enrollment trigger hota hai (Kafka se).

**Frontend pages:** `/cart`, `/student/cart`, `/student/payment`, `/student/wishlist`, `useCartStore`, `useWishlistStore`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `payment-service` |
| Port | `8089` |
| Base URL | `http://localhost:8080/api/payments` |
| Direct URL | `http://localhost:8089` |
| Total APIs | **14** |
| Database | `lms_payments` (PostgreSQL) |
| Kafka Topics (Produce) | `payment.success`, `payment.failed`, `enrollment.created` |
| Kafka Topics (Consume) | None |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **catalog-service** | REST | Track/course price fetch |
| **enrollment-service** | Kafka produce `payment.success` | Auto-enroll after payment |
| **notification-service** | Kafka produce | Payment receipt email |
| **admin-service** | REST | Financial transactions for admin |
| **analytics-service** | Kafka | Revenue tracking |

**Payment Gateway:** Razorpay / Stripe integration

---

## Kya Functionality Add Karni Hai

- [ ] Shopping cart CRUD (add/remove/update track items)
- [ ] Wishlist CRUD
- [ ] Checkout flow with GST calculation (18%)
- [ ] Razorpay/Stripe payment initiation
- [ ] Payment webhook handler (success/failure)
- [ ] Order history for student
- [ ] Invoice generation (PDF)
- [ ] Coupon/discount code support
- [ ] Refund processing (admin triggered)
- [ ] Cart merge on login (guest → authenticated)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `cart_items` | id, user_id, track_id, course_id, item_type, added_at |
| `wishlist_items` | id, user_id, track_id, course_id, added_at |
| `orders` | id, user_id, order_number, subtotal, gst, total, status, payment_id, created_at |
| `order_items` | id, order_id, track_id, course_id, title, price |
| `payments` | id, order_id, gateway, gateway_payment_id, amount, status, paid_at |
| `coupons` | id, code, discount_pct, max_uses, used_count, expires_at |

---

## API Endpoints (Total: 14)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/payments/cart` | Cart items fetch | Student |
| 2 | POST | `/api/payments/cart/items` | Cart mein item add | Student |
| 3 | PUT | `/api/payments/cart/items/{itemId}` | Cart item update | Student |
| 4 | DELETE | `/api/payments/cart/items/{itemId}` | Cart se remove | Student |
| 5 | DELETE | `/api/payments/cart` | Poori cart clear | Student |
| 6 | GET | `/api/payments/wishlist` | Wishlist fetch | Student |
| 7 | POST | `/api/payments/wishlist/items` | Wishlist mein add | Student |
| 8 | DELETE | `/api/payments/wishlist/items/{itemId}` | Wishlist se remove | Student |
| 9 | POST | `/api/payments/checkout` | Checkout summary calculate | Student |
| 10 | POST | `/api/payments/initiate` | Payment gateway start | Student |
| 11 | POST | `/api/payments/webhook` | Gateway callback (Razorpay/Stripe) | Internal |
| 12 | GET | `/api/payments/orders/me` | Meri orders history | Student |
| 13 | GET | `/api/payments/orders/{orderId}` | Order detail | Student |
| 14 | GET | `/api/payments/orders/{orderId}/invoice` | Invoice download | Student |

---

## Payment Flow

```
Student selects track → POST /checkout → POST /initiate
→ Razorpay payment page → Webhook POST /webhook
→ Kafka: payment.success → enrollment-service enrolls student
→ Frontend: /student/payment?status=success
```

---

## Frontend Integration

```javascript
// useCartStore replacement:
GET  /api/payments/cart
POST /api/payments/cart/items  { trackId, itemType: "track" }

// CoursePaymentPage.jsx:
POST /api/payments/checkout  { trackId }
POST /api/payments/initiate  { orderId }
```
