# Admin Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **platform administration** handle karegi — course approvals, financials/transactions, system settings, platform reports. Sirf admin role access kar sakta hai.

**Frontend pages:** `/admin/approvals`, `/admin/revenue`, `/admin/settings`, `/admin/reports`, `/admin/dashboard`

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `admin-service` |
| Port | `8094` |
| Base URL | `http://localhost:8080/api/admin` |
| Direct URL | `http://localhost:8094` |
| Total APIs | **12** |
| Database | `lms_admin` (PostgreSQL) |
| Kafka Topics (Produce) | `course.approved`, `course.rejected` |
| Kafka Topics (Consume) | `payment.success` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **content-service** | REST | Pending courses fetch for approval |
| **catalog-service** | Kafka produce `course.approved` | Approved course publish |
| **payment-service** | REST + Kafka | Financial transactions |
| **user-service** | REST | User management data |
| **analytics-service** | REST | Dashboard & reports |
| **notification-service** | Kafka produce | Approve/reject notifications to mentor |

---

## Kya Functionality Add Karni Hai

- [ ] Course approval queue (pending/approved/rejected)
- [ ] Approve course → catalog mein publish
- [ ] Reject course with reason → mentor ko notification
- [ ] Financial dashboard: total revenue, platform cut, mentor payouts
- [ ] Transaction history with filters
- [ ] Mentor payout processing
- [ ] Refund management
- [ ] System settings (platform name, GST rate, commission %)
- [ ] Platform reports generation
- [ ] CSV export for financials
- [ ] Audit log for admin actions

---

## Database Tables

| Table | Columns |
|-------|---------|
| `course_approvals` | id, course_id, mentor_id, status, submitted_at, reviewed_at, reviewed_by, rejection_reason |
| `platform_settings` | key, value, updated_at, updated_by |
| `financial_transactions` | id, type, amount, platform_cut, user_id, reference_id, created_at |
| `mentor_payouts` | id, mentor_id, amount, period, status, processed_at |
| `admin_audit_log` | id, admin_id, action, entity, entity_id, details, created_at |

---

## API Endpoints (Total: 12)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/admin/approvals/courses` | Pending courses list | Admin |
| 2 | GET | `/api/admin/approvals/courses/{courseId}` | Course approval detail | Admin |
| 3 | POST | `/api/admin/approvals/courses/{courseId}/approve` | Course approve karo | Admin |
| 4 | POST | `/api/admin/approvals/courses/{courseId}/reject` | Course reject (reason required) | Admin |
| 5 | GET | `/api/admin/financials/summary` | Revenue summary dashboard | Admin |
| 6 | GET | `/api/admin/financials/transactions` | Transaction history | Admin |
| 7 | GET | `/api/admin/financials/payouts` | Mentor payouts list | Admin |
| 8 | POST | `/api/admin/financials/payouts` | Process mentor payout | Admin |
| 9 | GET | `/api/admin/settings` | Platform settings fetch | Admin |
| 10 | PUT | `/api/admin/settings` | Settings update | Admin |
| 11 | GET | `/api/admin/reports/platform` | Platform overview report | Admin |
| 12 | POST | `/api/admin/reports/generate` | Custom report generate | Admin |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `CourseApprovalsPage.jsx` | GET/POST `/api/admin/approvals/courses/*` |
| `FinancialsPage.jsx` | GET `/api/admin/financials/*` |
| `SystemSettingsPage.jsx` | GET/PUT `/api/admin/settings` |
| `AdminReportsPage.jsx` | GET `/api/admin/reports/platform` |

---

## Default Platform Settings

| Key | Default Value |
|-----|---------------|
| `platform.name` | Cloud Nexus |
| `platform.gst_rate` | 0.18 |
| `platform.commission_pct` | 0.30 |
| `platform.currency` | INR |
| `platform.support_email` | support@cloudnexus.com |
