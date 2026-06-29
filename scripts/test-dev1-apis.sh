#!/usr/bin/env bash
set -euo pipefail

A=http://localhost:8081
U=http://localhost:8082
D=http://localhost:8094
PASS=0
FAIL=0
TOKEN=""
REFRESH=""

check() {
  local method=$1 url=$2 expected=$3
  shift 3
  local code
  code=$(curl -s -o /tmp/dev1_body.json -w "%{http_code}" -X "$method" "$@" "$url")
  if [ "$code" = "$expected" ]; then
    echo "✅ $method $url → $code"
    PASS=$((PASS+1))
  else
    echo "❌ $method $url → $code (expected $expected)"
    head -c 300 /tmp/dev1_body.json 2>/dev/null; echo
    FAIL=$((FAIL+1))
  fi
}

echo "========== AUTH (12) =========="
check GET "$A/api/auth/health" 200
check POST "$A/api/auth/register" 200 -H "Content-Type: application/json" \
  -d '{"fullName":"Test Student","email":"test.student.'$(date +%s)'@example.com","password":"Password123!"}'

LOGIN=$(curl -s -X POST "$A/api/auth/login" -H "Content-Type: application/json" \
  -d '{"email":"admin@cloudnexus.com","password":"Password123!","rememberMe":false}')
TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('accessToken',''))" 2>/dev/null || true)
REFRESH=$(echo "$LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin).get('refreshToken',''))" 2>/dev/null || true)

if [ -n "$TOKEN" ]; then
  echo "✅ POST $A/api/auth/login → 200 (token received)"
  PASS=$((PASS+1))
else
  echo "❌ POST $A/api/auth/login → failed"
  FAIL=$((FAIL+1))
fi

check GET "$A/api/auth/me" 200 -H "Authorization: Bearer $TOKEN"
check POST "$A/api/auth/validate-token" 200 -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}"
check POST "$A/api/auth/refresh-token" 200 -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
check POST "$A/api/auth/forgot-password" 200 -H "Content-Type: application/json" \
  -d '{"email":"alex.chen@example.com"}'
check POST "$A/api/auth/resend-otp" 200 -H "Content-Type: application/json" \
  -d '{"email":"alex.chen@example.com","purpose":"PASSWORD_RESET"}'

OTP=$(PGPASSWORD=lms_secret psql -h localhost -p 15432 -U lms_admin -d lms_auth -tAc \
  "SELECT code FROM otp_codes WHERE email='alex.chen@example.com' ORDER BY expires_at DESC LIMIT 1" 2>/dev/null | tr -d '[:space:]' || echo "")

if [ -n "$OTP" ]; then
  check POST "$A/api/auth/verify-otp" 200 -H "Content-Type: application/json" \
    -d "{\"email\":\"alex.chen@example.com\",\"code\":\"$OTP\",\"purpose\":\"PASSWORD_RESET\"}"
  check POST "$A/api/auth/reset-password" 200 -H "Content-Type: application/json" \
    -d "{\"email\":\"alex.chen@example.com\",\"code\":\"$OTP\",\"purpose\":\"PASSWORD_RESET\",\"newPassword\":\"Password123!\"}"
else
  echo "⚠️  Skipping OTP verify/reset (no DB OTP found)"
fi

check POST "$A/api/auth/change-password" 200 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"currentPassword":"Password123!","newPassword":"Password123!"}'
check POST "$A/api/auth/logout" 200 -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"

echo "========== USER (14) =========="
check GET "$U/api/users/health" 200
check GET "$U/api/users/profile" 200 -H "X-User-Id: 1"
check PUT "$U/api/users/profile" 200 -H "X-User-Id: 1" -H "Content-Type: application/json" \
  -d '{"fullName":"Alex Chen","bio":"Backend learner","phone":"+91-9876543210"}'
check PATCH "$U/api/users/profile/avatar" 200 -H "X-User-Id: 1" -H "Content-Type: application/json" \
  -d '{"avatarUrl":"https://randomuser.me/api/portraits/men/32.jpg"}'
check GET "$U/api/users/profile/settings" 200 -H "X-User-Id: 1"
check PUT "$U/api/users/profile/settings" 200 -H "X-User-Id: 1" -H "Content-Type: application/json" \
  -d '{"theme":"dark","language":"en","emailNotifications":true,"pushNotifications":false}'
check GET "$U/api/users" 200 -H "X-User-Role: ADMIN"
check GET "$U/api/users/1" 200 -H "X-User-Role: ADMIN"
check POST "$U/api/users" 200 -H "X-User-Role: ADMIN" -H "Content-Type: application/json" \
  -d '{"email":"new.user.'$(date +%s)'@example.com","fullName":"New User","role":"STUDENT"}'
check PUT "$U/api/users/1" 200 -H "X-User-Role: ADMIN" -H "Content-Type: application/json" \
  -d '{"fullName":"Alex Chen","bio":"Updated via admin"}'
check PATCH "$U/api/users/5/status" 200 -H "X-User-Role: ADMIN" -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}'
check GET "$U/api/users/mentors" 200 -H "X-User-Role: ADMIN"
check POST "$U/api/users/mentors" 200 -H "X-User-Role: ADMIN" -H "Content-Type: application/json" \
  -d '{"fullName":"New Mentor","username":"newmentor","email":"mentor.'$(date +%s)'@cloudnexus.com","password":"Password123!","professionalRole":"Senior Engineer","company":"Cloud Nexus","trackLabel":"Cloud & DevOps","location":"Bangalore","bio":"Teaching cloud."}'
check GET "$U/api/users/students/1/summary" 200 -H "X-User-Role: ADMIN"

echo "========== ADMIN (12) =========="
check GET "$D/api/admin/health" 200
check GET "$D/api/admin/approvals/courses" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"

PENDING_APPROVE=$(curl -s "$D/api/admin/approvals/courses?status=Pending" -H "X-User-Id: 4" -H "X-User-Role: ADMIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else 'C-8290')" 2>/dev/null || echo "C-8290")
PENDING_REJECT=$(curl -s "$D/api/admin/approvals/courses?status=Pending" -H "X-User-Id: 4" -H "X-User-Role: ADMIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[1]['id'] if len(d)>1 else (d[0]['id'] if d else 'C-8292'))" 2>/dev/null || echo "C-8292")

check GET "$D/api/admin/approvals/courses/$PENDING_APPROVE" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check POST "$D/api/admin/approvals/courses/$PENDING_APPROVE/approve" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check POST "$D/api/admin/approvals/courses/$PENDING_REJECT/reject" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" -d '{"reason":"Content needs more hands-on labs"}'
check GET "$D/api/admin/financials/summary" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check GET "$D/api/admin/financials/transactions" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check GET "$D/api/admin/financials/payouts" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check POST "$D/api/admin/financials/payouts" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" -d '{"mentorId":2,"amount":1500,"period":"2026-06"}'
check GET "$D/api/admin/settings" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check PUT "$D/api/admin/settings" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" -d '{"platformName":"Cloud Nexus","commissionPct":30,"gstRate":0.18,"currency":"INR","supportEmail":"support@cloudnexus.com"}'
check GET "$D/api/admin/reports/platform" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN"
check POST "$D/api/admin/reports/generate" 200 -H "X-User-Id: 4" -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" -d '{"reportType":"financial","fromDate":"2026-01-01","toDate":"2026-06-30"}'

echo ""
echo "========== RESULT: $PASS passed, $FAIL failed =========="
exit $FAIL
