# Certificate Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **certificates issue aur verify** karegi. Jab student track 100% complete karta hai, automatically certificate generate hoga. Student apne certificates download kar sakta hai. Public verification link bhi hoga.

**Frontend pages:** `/student/certificates`, `/student/certificates/:id`, Certificate PDF download

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `certificate-service` |
| Port | `8090` |
| Base URL | `http://localhost:8080/api/certificates` |
| Direct URL | `http://localhost:8090` |
| Total APIs | **6** |
| Database | `lms_certificates` (PostgreSQL) |
| Kafka Topics (Consume) | `track.completed` |
| Kafka Topics (Produce) | `certificate.issued` |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **enrollment-service** | Kafka consume `track.completed` | Auto-generate certificate |
| **user-service** | REST | Recipient name |
| **catalog-service** | REST | Track/course title |
| **mentor-service** | REST | Mentor name on certificate |
| **notification-service** | Kafka produce `certificate.issued` | Congrats email |
| **media-service** | REST | PDF storage |

---

## Kya Functionality Add Karni Hai

- [ ] Auto certificate generation on track completion
- [ ] Unique certificate ID (e.g., CN-AWSA-8412)
- [ ] Public verification page (`/verify/{code}`)
- [ ] Certificate PDF generation (html2canvas/jspdf logic backend pe)
- [ ] Certificate list for student
- [ ] Certificate detail with verify link
- [ ] Share certificate (LinkedIn, Twitter link)
- [ ] Certificate template management (admin)
- [ ] Revoke certificate (admin, fraud cases)

---

## Database Tables

| Table | Columns |
|-------|---------|
| `certificates` | id, code, user_id, track_id, title, recipient_name, issue_date, duration, mentor_name, status, verify_url, pdf_url |
| `certificate_templates` | id, track_id, template_html, logo_url |

---

## API Endpoints (Total: 6)

| # | Method | Endpoint | Description | Auth |
|---|--------|----------|-------------|------|
| 1 | GET | `/api/certificates/me` | Meri saari certificates | Student |
| 2 | GET | `/api/certificates/{certificateId}` | Certificate detail | Student |
| 3 | GET | `/api/certificates/verify/{certificateCode}` | Public verification | No |
| 4 | POST | `/api/certificates/generate` | Manual generate (internal/admin) | Internal |
| 5 | GET | `/api/certificates/{certificateId}/download` | PDF download | Student |
| 6 | POST | `/api/certificates/{certificateId}/share` | Share link generate | Student |

---

## Frontend Data Mapping

| Frontend | API |
|----------|-----|
| `src/data/certificates.js` | GET `/api/certificates/me` |
| `CertificatesPage.jsx` | GET `/api/certificates/me` |
| `CertificateDetailPage.jsx` | GET `/api/certificates/{id}` |
| `downloadCertificatePdf.js` | GET `/api/certificates/{id}/download` |
