# Media Service

## Service Ka Goal (Kya Kaam Karegi)

Yeh service **file uploads** handle karegi — course thumbnails, lesson videos, assignment files, avatars, PDFs. Files cloud storage (S3/Azure Blob) mein store hongi aur presigned URLs return hongi.

**Frontend pages:** `/mentor/upload` (thumbnail, video), `/student/assignments` (file submit), profile avatar upload

---

## Service Details

| Property | Value |
|----------|-------|
| Service Name | `media-service` |
| Port | `8095` |
| Base URL | `http://localhost:8080/api/media` |
| Direct URL | `http://localhost:8095` |
| Total APIs | **8** |
| Database | `lms_media` (PostgreSQL — metadata only) |
| Storage | AWS S3 / Azure Blob Storage |

---

## Connected Services

| Service | Connection | Kyun |
|---------|------------|------|
| **content-service** | Called by | Course thumbnails, lesson videos |
| **user-service** | Called by | Avatar uploads |
| **assessment-service** | Called by | Assignment submission files |
| **certificate-service** | Called by | Certificate PDF storage |

---

## Kya Functionality Add Karni Hai

- [ ] Image upload (thumbnail, avatar) — max 5MB
- [ ] Video upload (lesson videos) — chunked upload for large files
- [ ] Document upload (PDF, ZIP for assignments) — max 50MB
- [ ] Presigned URL generation for secure access
- [ ] File metadata storage (name, size, type, owner)
- [ ] Image resize/optimize on upload
- [ ] Video transcoding queue (future — HLS streaming)
- [ ] File delete (soft delete + storage cleanup)
- [ ] Virus scan on upload (ClamAV)
- [ ] CDN integration for public assets

---

## Database Tables

| Table | Columns |
|-------|---------|
| `media_files` | id, file_name, file_type, mime_type, size_bytes, storage_key, storage_url, uploaded_by, entity_type, entity_id, created_at, deleted_at |

---

## API Endpoints (Total: 8)

| # | Method | Endpoint | Description | Role |
|---|--------|----------|-------------|------|
| 1 | POST | `/api/media/upload/image` | Image upload (multipart) | Any |
| 2 | POST | `/api/media/upload/video` | Video upload (multipart/chunked) | Mentor |
| 3 | POST | `/api/media/upload/document` | Document upload (PDF, ZIP) | Any |
| 4 | GET | `/api/media/files/{fileId}` | File metadata fetch | Any |
| 5 | DELETE | `/api/media/files/{fileId}` | File delete | Owner, Admin |
| 6 | POST | `/api/media/upload/course-thumbnail` | Course thumbnail (optimized) | Mentor |
| 7 | POST | `/api/media/upload/avatar` | User avatar (resized 200x200) | Any |
| 8 | GET | `/api/media/files/{fileId}/presigned-url` | Temporary access URL | Any |

---

## Upload Limits

| Type | Max Size | Allowed Formats |
|------|----------|-----------------|
| Image | 5 MB | jpg, png, webp, svg |
| Video | 2 GB | mp4, webm, mov |
| Document | 50 MB | pdf, zip, docx |

---

## Storage Structure

```
s3://lms-media/
├── avatars/{userId}/{filename}
├── courses/{courseId}/thumbnail.{ext}
├── lessons/{lessonId}/video.{ext}
├── assignments/{submissionId}/{filename}
└── certificates/{certificateId}.pdf
```
