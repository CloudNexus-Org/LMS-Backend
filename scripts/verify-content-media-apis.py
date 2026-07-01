#!/usr/bin/env python3
"""Verify all content-service (18) and media-service (8) APIs."""
import json
import sys
import urllib.error
import urllib.request
from io import BytesIO

CONTENT = "http://localhost:8085"
MEDIA = "http://localhost:8095"
GATEWAY = "http://localhost:8080"

MENTOR = {"X-User-Id": "2", "X-User-Role": "MENTOR"}
STUDENT = {"X-User-Id": "3", "X-User-Role": "STUDENT"}


def req(method, url, data=None, headers=None, body=None, content_type=None):
    h = dict(headers or {})
    payload = body
    if data is not None and body is None:
        payload = json.dumps(data).encode()
        h.setdefault("Content-Type", "application/json")
    if content_type:
        h["Content-Type"] = content_type
    r = urllib.request.Request(url, data=payload, headers=h, method=method)
    try:
        with urllib.request.urlopen(r, timeout=15) as res:
            raw = res.read()
            if not raw:
                return res.status, None
            try:
                return res.status, json.loads(raw.decode())
            except json.JSONDecodeError:
                return res.status, raw.decode()
    except urllib.error.HTTPError as e:
        body_text = e.read().decode()
        try:
            return e.code, json.loads(body_text)
        except json.JSONDecodeError:
            return e.code, body_text


def multipart(url, fields, files, headers=None):
    boundary = "----LmsVerifyBoundary7"
    h = dict(headers or {})
    h["Content-Type"] = f"multipart/form-data; boundary={boundary}"
    parts = []
    for name, value in fields.items():
        parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"\r\n\r\n{value}\r\n".encode())
    for name, (fname, content, mime) in files.items():
        parts.append(
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"; filename=\"{fname}\"\r\nContent-Type: {mime}\r\n\r\n".encode()
        )
        parts.append(content)
        parts.append(b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    body = b"".join(parts)
    r = urllib.request.Request(url, data=body, headers=h, method="POST")
    try:
        with urllib.request.urlopen(r, timeout=15) as res:
            return res.status, json.loads(res.read().decode())
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


results = []
state = {}


def check(name, ok, detail=""):
    results.append((name, ok, detail))
    mark = "PASS" if ok else "FAIL"
    print(f"  {mark} {name}" + (f" — {detail}" if detail else ""))


print("\n=== CONTENT SERVICE (18 APIs) ===\n")

s, h = req("GET", f"{CONTENT}/api/content/health")
check("01 GET /health", s == 200 and h.get("status") == "UP", str(h))

s, course = req("POST", f"{CONTENT}/api/content/courses", {
    "title": "Verification Test Course Title",
    "description": "Long enough description for automated API verification of content service endpoints.",
    "category": "Cloud & DevOps", "level": "Beginner", "language": "English", "trackId": "cloud"
}, MENTOR)
cid = course.get("id") if isinstance(course, dict) else None
state["courseId"] = cid
check("02 POST /courses", s == 200 and cid, f"id={cid}")

s, updated = req("PUT", f"{CONTENT}/api/content/courses/{cid}", {"subtitle": "Verified subtitle"}, MENTOR)
check("03 PUT /courses/{id}", s == 200 and updated.get("subtitle") == "Verified subtitle")

s, detail = req("GET", f"{CONTENT}/api/content/courses/{cid}", headers=MENTOR)
check("04 GET /courses/{id}", s == 200 and detail.get("id") == cid)

s, mod = req("POST", f"{CONTENT}/api/content/courses/{cid}/modules", {"title": "Verify Module"}, MENTOR)
mid = mod.get("id") if isinstance(mod, dict) else None
state["moduleId"] = mid
check("05 POST /modules", s == 200 and mid, f"id={mid}")

s, mod2 = req("PUT", f"{CONTENT}/api/content/courses/{cid}/modules/{mid}", {"title": "Verify Module Updated"}, MENTOR)
check("06 PUT /modules/{id}", s == 200 and mod2.get("title") == "Verify Module Updated")

s, lesson = req("POST", f"{CONTENT}/api/content/courses/{cid}/modules/{mid}/lessons", {
    "title": "Verify Lesson", "type": "video", "durationMin": 8, "previewFree": True
}, MENTOR)
lid = lesson.get("id") if isinstance(lesson, dict) else None
state["lessonId"] = lid
check("07 POST /lessons", s == 200 and lid, f"id={lid}")

s, lesson2 = req("PUT", f"{CONTENT}/api/content/courses/{cid}/lessons/{lid}", {"summary": "Verified"}, MENTOR)
check("08 PUT /lessons/{id}", s == 200 and lesson2.get("summary") == "Verified")

s, reordered = req("PUT", f"{CONTENT}/api/content/courses/{cid}/curriculum/reorder", {
    "modules": [{"moduleId": mid, "orderIndex": 0, "lessons": [{"lessonId": lid, "orderIndex": 0}]}]
}, MENTOR)
check("09 PUT /curriculum/reorder", s == 200 and reordered.get("id") == cid)

s, submitted = req("POST", f"{CONTENT}/api/content/courses/{cid}/submit-for-approval", {}, MENTOR)
check("10 POST /submit-for-approval", s == 200 and submitted.get("status") == "PENDING")

s, drafts = req("GET", f"{CONTENT}/api/content/courses/drafts", headers=MENTOR)
check("11 GET /courses/drafts", s == 200 and isinstance(drafts, list) and len(drafts) > 0, f"count={len(drafts) if isinstance(drafts,list) else 0}")

s, track = req("GET", f"{CONTENT}/api/content/tracks/cloud/lessons")
check("12 GET /tracks/{trackId}/lessons", s == 200 and isinstance(track, list) and len(track) > 0, f"count={len(track)}")

lesson_for_read = track[0]["id"] if track else 1
s, ld = req("GET", f"{CONTENT}/api/content/lessons/{lesson_for_read}")
check("13 GET /lessons/{id}", s == 200 and ld.get("id") == lesson_for_read, ld.get("title", ""))

s, resources = req("GET", f"{CONTENT}/api/content/lessons/1/resources")
check("14 GET /lessons/{id}/resources", s == 200 and isinstance(resources, list), f"count={len(resources)}")

s, transcript = req("GET", f"{CONTENT}/api/content/lessons/1/transcript")
check("15 GET /lessons/{id}/transcript", s == 200 and transcript.get("lessonId") == 1, f"lines={len(transcript.get('lines',[]))}")

s, priced = req("PATCH", f"{CONTENT}/api/content/courses/{cid}/pricing", {"pricingPlan": "paid", "price": 4999}, MENTOR)
# PATCH might not be supported by http helper - let me check - I used PATCH in script but req only has method param - good

# Actually I need to fix - I used PATCH in the script but the req function supports any method. Good.

check("16 PATCH /courses/{id}/pricing", s == 200 and priced.get("pricingPlan") == "paid")

# Find approved course for publish (seed id 3)
s, pub = req("POST", f"{CONTENT}/api/content/courses/3/publish", {}, MENTOR)
check("17 POST /courses/{id}/publish", s == 200 and pub.get("status") == "PUBLISHED", pub.get("status", str(pub)))

s, del_lesson = req("DELETE", f"{CONTENT}/api/content/courses/{cid}/lessons/{lid}", headers=MENTOR)
check("18 DELETE /lessons/{id}", s == 200 and isinstance(del_lesson, dict) and "message" in del_lesson)

s, del_mod = req("DELETE", f"{CONTENT}/api/content/courses/{cid}/modules/{mid}", headers=MENTOR)
check("19 DELETE /modules/{id}", s == 200 and isinstance(del_mod, dict) and "message" in del_mod)

print("\n=== MEDIA SERVICE (8 APIs) ===\n")

s, h = req("GET", f"{MEDIA}/api/media/health")
check("M1 GET /health", s == 200 and h.get("status") == "UP", str(h))

png = bytes([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0])
jpg = bytes([0xFF, 0xD8, 0xFF, 0xE0, 0, 0x10, 0x4A, 0x46, 0x49, 0x46, 0, 1])
pdf = b"%PDF-1.4\n% verify"

s, img = multipart(f"{MEDIA}/api/media/upload/image", {}, {"file": ("verify.png", png, "image/png")}, STUDENT)
fid = img.get("id") if isinstance(img, dict) else None
state["fileId"] = fid
check("M2 POST /upload/image", s == 200 and fid, f"id={fid}")

s, vid = multipart(f"{MEDIA}/api/media/upload/video", {}, {"file": ("clip.mp4", b"fake-mp4-content", "video/mp4")}, MENTOR)
check("M3 POST /upload/video", s == 200 and vid.get("fileType") == "video")

s, doc = multipart(f"{MEDIA}/api/media/upload/document", {}, {"file": ("notes.pdf", pdf, "application/pdf")}, STUDENT)
check("M4 POST /upload/document", s == 200 and doc.get("fileType") == "document")

s, meta = req("GET", f"{MEDIA}/api/media/files/{fid}")
check("M5 GET /files/{id}", s == 200 and meta.get("id") == fid)

s, thumb = multipart(f"{MEDIA}/api/media/upload/course-thumbnail?courseId=1", {}, {"file": ("thumb.jpg", jpg, "image/jpeg")}, MENTOR)
check("M6 POST /upload/course-thumbnail", s == 200 and thumb.get("fileType") == "course-thumbnail")

s, avatar = multipart(f"{MEDIA}/api/media/upload/avatar", {}, {"file": ("avatar.png", png, "image/png")}, STUDENT)
check("M7 POST /upload/avatar", s == 200 and avatar.get("fileType") == "avatar")

s, presigned = req("GET", f"{MEDIA}/api/media/files/{fid}/presigned-url")
check("M8 GET /files/{id}/presigned-url", s == 200 and presigned.get("url"), presigned.get("url", "")[:60])

s, deleted = req("DELETE", f"{MEDIA}/api/media/files/{fid}", headers=STUDENT)
check("M9 DELETE /files/{id}", s == 200 and deleted.get("id") == fid)

print("\n=== GATEWAY ROUTES (sample) ===\n")

try:
    gw_c = urllib.request.urlopen(f"{GATEWAY}/api/content/health", timeout=10).status
    check("GW content /health", gw_c == 200, str(gw_c))
except Exception as e:
    check("GW content /health", False, str(e))

try:
    gw_m = urllib.request.urlopen(f"{GATEWAY}/api/media/health", timeout=10).status
    check("GW media /health", gw_m == 200, str(gw_m))
except Exception as e:
    check("GW media /health", False, str(e))

passed = sum(1 for _, ok, _ in results if ok)
total = len(results)
print(f"\n{'='*50}")
print(f"RESULT: {passed}/{total} passed")
print(f"{'='*50}\n")
sys.exit(0 if passed == total else 1)
