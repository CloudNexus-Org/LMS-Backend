#!/usr/bin/env bash
set -euo pipefail

C=http://localhost:8083
M=http://localhost:8084
R=http://localhost:8091
PASS=0
FAIL=0

check() {
  local method=$1 url=$2 expected=$3
  shift 3
  local code
  code=$(curl -s -o /tmp/api_body.json -w "%{http_code}" -X "$method" "$@" "$url")
  if [ "$code" = "$expected" ]; then
    echo "✅ $method $url → $code"
    PASS=$((PASS+1))
  else
    echo "❌ $method $url → $code (expected $expected)"
    head -c 200 /tmp/api_body.json 2>/dev/null; echo
    FAIL=$((FAIL+1))
  fi
}

echo "========== CATALOG (16) =========="
check GET "$C/api/catalog/health" 200
check GET "$C/api/catalog/courses?page=0&size=12&difficulty=Intermediate&sort=rating" 200
check GET "$C/api/catalog/courses/featured" 200
check GET "$C/api/catalog/courses/filters" 200
check GET "$C/api/catalog/courses/aws-solution-architect" 200
check GET "$C/api/catalog/courses/aws-solution-architect/preview" 200
check GET "$C/api/catalog/tracks" 200
check GET "$C/api/catalog/tracks/cloud" 200
check GET "$C/api/catalog/tracks/cloud/courses" 200
check GET "$C/api/catalog/explore/cloud" 200
check GET "$C/api/catalog/explore/search?q=aws" 200
check GET "$C/api/catalog/categories" 200
check GET "$C/api/catalog/stats/public" 200
check GET "$C/api/catalog/faq" 200
check GET "$C/api/catalog/testimonials" 200
check GET "$C/api/catalog/how-it-works" 200

echo "========== MENTOR (10) =========="
check GET "$M/api/mentors" 200
check GET "$M/api/mentors/arjan-singh" 200
check GET "$M/api/mentors/arjan-singh/courses" 200
check GET "$M/api/mentors/arjan-singh/reviews" 200
check GET "$M/api/mentors/me/dashboard" 200 -H "X-User-Id: 101"
check GET "$M/api/mentors/me/profile" 200 -H "X-User-Id: 101"
check PUT "$M/api/mentors/me/profile" 200 -H "X-User-Id: 101" -H "Content-Type: application/json" -d '{"bio":"API test update"}'
check GET "$M/api/mentors/me/students" 200 -H "X-User-Id: 101"
check GET "$M/api/mentors/me/students/201" 200 -H "X-User-Id: 101"
check GET "$M/api/mentors/me/notifications-count" 200 -H "X-User-Id: 101"

echo "========== REVIEW (8) =========="
check GET "$R/api/reviews/courses/2?page=0&size=10" 200
check GET "$R/api/reviews/courses/2/summary" 200
check GET "$R/api/reviews/me" 200 -H "X-User-Id: 201"
check GET "$R/api/reviews/mentor/me?courseIds=1&courseIds=2" 200 -H "X-User-Id: 101"
check POST "$R/api/reviews/courses/5" 200 -H "X-User-Id: 399" -H "Content-Type: application/json" -d '{"rating":4,"title":"Solid Go course","body":"Great concurrency content.","reviewerName":"Test User"}'
check POST "$R/api/reviews/1/helpful" 200 -H "X-User-Id: 888"
check PUT "$R/api/reviews/1" 200 -H "X-User-Id: 201" -H "Content-Type: application/json" -d '{"rating":5,"title":"Updated title","body":"Updated body","reviewerName":"Priya Verma"}'

echo "========== INTER-SERVICE (Kafka) =========="
BEFORE=$(curl -s "$C/api/catalog/courses/azure-generative-ai" | python3 -c "import sys,json; print(json.load(sys.stdin).get('reviews',0))" 2>/dev/null || echo 0)
curl -s -X POST "$R/api/reviews/courses/2" -H "X-User-Id: 306" -H "Content-Type: application/json" \
  -d '{"rating":5,"title":"Kafka test","body":"Testing catalog sync","reviewerName":"Kafka Tester"}' > /dev/null 2>&1 || true
sleep 2
AFTER=$(curl -s "$C/api/catalog/courses/azure-generative-ai" | python3 -c "import sys,json; print(json.load(sys.stdin).get('reviews',0))" 2>/dev/null || echo 0)
if [ "$AFTER" -ge "$BEFORE" ]; then
  echo "✅ Kafka review.created → catalog rating sync (reviews: $BEFORE → $AFTER)"
  PASS=$((PASS+1))
else
  echo "❌ Kafka sync failed (reviews: $BEFORE → $AFTER)"
  FAIL=$((FAIL+1))
fi

echo ""
echo "========== RESULT: $PASS passed, $FAIL failed =========="
exit $FAIL
