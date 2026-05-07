#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8888}"
EMAIL="${JMETER_EMAIL:-student@gmail.com}"
PASSWORD="${JMETER_PASSWORD:-abcd@1234}"
PLAN_FILE="$ROOT_DIR/tooling/jmeter/course-purchase-flow.jmx"
JTL_FILE="$ROOT_DIR/evidence/jmeter/run-auth-latest.jtl"
DASHBOARD_DIR="$ROOT_DIR/evidence/jmeter/dashboard-auth-latest"

mkdir -p "$ROOT_DIR/evidence/jmeter"
rm -rf "$JTL_FILE" "$DASHBOARD_DIR"

login_json=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

token=$(printf '%s' "$login_json" | jq -r '.data.token // empty')
if [[ -z "$token" ]]; then
  echo "Failed to get access token from login response." >&2
  echo "$login_json" >&2
  exit 1
fi

course_json=$(curl -s -X POST "$BASE_URL/api/v1/course/get-courses?page=0&size=50&sort=createdAt,desc" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{}')

cart_json=$(curl -s -X GET "$BASE_URL/api/v1/cart" \
  -H "Authorization: Bearer $token")

cart_course_ids=$(printf '%s' "$cart_json" | jq -r '.data[]?.course.id // empty')

add_course_id=$(printf '%s' "$course_json" | jq -r '.data.content[] | select(.isBought != true) | .id' | while read -r cid; do
  if [[ -n "$cid" ]] && ! grep -qx "$cid" <<<"$cart_course_ids"; then
    echo "$cid"
    break
  fi
done)

if [[ -z "$add_course_id" ]]; then
  add_course_id=$(printf '%s' "$course_json" | jq -r '.data.content[] | select(.isBought != true) | .id' | head -n 1)
fi

order_course_ids=$(printf '%s' "$course_json" | jq -r '.data.content[] | select(.isBought != true and (.isOrdered == null)) | .id' | head -n 10 | paste -sd ';' -)
if [[ -z "$order_course_ids" ]]; then
  order_course_ids=$(printf '%s' "$course_json" | jq -r '.data.content[] | select(.isBought != true) | .id' | head -n 10 | paste -sd ';' -)
fi

if [[ -z "$add_course_id" || -z "$order_course_ids" ]]; then
  echo "Could not find usable add/order course ids from /course/get-courses." >&2
  exit 1
fi

pending_order_json=$(curl -s -X POST "$BASE_URL/api/v1/order?page=0&size=20&sort=createdAt,desc&statusOrder=PENDING" \
  -H "Authorization: Bearer $token" \
  -H "Content-Type: application/json" \
  -d '{}')

order_id=$(printf '%s' "$pending_order_json" | jq -r '.data.content[0].id // empty')
if [[ -z "$order_id" ]]; then
  first_order_course_id=$(printf '%s' "$order_course_ids" | cut -d';' -f1)
  create_order_json=$(curl -s -X POST "$BASE_URL/api/v1/order/create-order-by-course?id=$first_order_course_id" \
    -H "Authorization: Bearer $token")
  order_id=$(printf '%s' "$create_order_json" | jq -r '.data.id // empty')
fi
if [[ -z "$order_id" ]]; then
  echo "Could not find or create a pending orderId for payment load." >&2
  exit 1
fi

echo "Using dynamic test data: addCourseId=$add_course_id, orderCourseIds=$order_course_ids, orderId=$order_id"

jmeter -n \
  -t "$PLAN_FILE" \
  -JaccessToken="$token" \
  -JaddCourseId="$add_course_id" \
  -JorderCourseIds="$order_course_ids" \
  -JorderId="$order_id" \
  -JstudentEmail="$EMAIL" \
  -JstudentPassword="$PASSWORD" \
  -l "$JTL_FILE" \
  -e -o "$DASHBOARD_DIR"

echo "JMeter completed. Dashboard: $DASHBOARD_DIR/index.html"
