#!/usr/bin/env bash
set -euo pipefail

BASE_CATALOG="http://localhost:8083"
BASE_MENTOR="http://localhost:8084"
BASE_REVIEW="http://localhost:8091"

echo "=== Catalog Service ==="
curl -s "$BASE_CATALOG/api/catalog/health" | head -c 200; echo
curl -s "$BASE_CATALOG/api/catalog/courses/featured" | head -c 300; echo
curl -s "$BASE_CATALOG/api/catalog/courses/aws-solution-architect" | head -c 300; echo
curl -s "$BASE_CATALOG/api/catalog/tracks/cloud" | head -c 300; echo

echo "=== Mentor Service ==="
curl -s "$BASE_MENTOR/api/mentors" | head -c 300; echo
curl -s "$BASE_MENTOR/api/mentors/arjan-singh" | head -c 300; echo
curl -s -H "X-User-Id: 101" "$BASE_MENTOR/api/mentors/me/dashboard" | head -c 300; echo

echo "=== Review Service ==="
curl -s "$BASE_REVIEW/api/reviews/courses/2/summary" | head -c 300; echo
curl -s -H "X-User-Id: 201" "$BASE_REVIEW/api/reviews/me" | head -c 300; echo

echo "All smoke tests completed."
