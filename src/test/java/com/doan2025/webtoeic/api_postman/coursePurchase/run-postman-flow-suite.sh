#!/usr/bin/env bash

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
COLLECTION_FILE="$SCRIPT_DIR/course-purchase-flow.postman_collection.json"
ENV_FILE="$SCRIPT_DIR/course-purchase-flow.local.postman_environment.json"
RESET_DB_SCRIPT="$ROOT_DIR/reset-test-db.sh"
NEWMAN_BIN=(npx -y newman)
BASE_ENV_VARS=(
  --env-var "baseUrl=http://localhost:8888"
  --env-var "studentEmail=student@gmail.com"
  --env-var "studentPassword=abcd@1234"
  --env-var "teacherEmail=teacher@gmail.com"
  --env-var "teacherPassword=abcd@1234"
  --env-var "otherUserOrderId=4801"
  --env-var "orderPage=0"
  --env-var "orderSize=10"
  --env-var "orderSort=createdAt,desc"
)

if [[ ! -f "$COLLECTION_FILE" ]]; then
  echo "Collection not found: $COLLECTION_FILE" >&2
  exit 1
fi

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

if [[ ! -x "$RESET_DB_SCRIPT" ]]; then
  echo "Reset script not found or not executable: $RESET_DB_SCRIPT" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required but was not found in PATH" >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required but was not found in PATH" >&2
  exit 1
fi

login_token() {
  local email="$1"
  local password="$2"
  curl -s -X POST "http://localhost:8888/api/v1/auth/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"${email}\",\"password\":\"${password}\"}" \
    | jq -r '.data.token'
}

echo "\n=== Resetting DB before OrderController suite ==="
"$RESET_DB_SCRIPT" reset >/dev/null

STUDENT_TOKEN="$(login_token "student@gmail.com" "abcd@1234")"
if [[ -z "$STUDENT_TOKEN" || "$STUDENT_TOKEN" == "null" ]]; then
  echo "Unable to obtain student token" >&2
  exit 1
fi

TEACHER_TOKEN="$(login_token "teacher@gmail.com" "abcd@1234")"
if [[ -z "$TEACHER_TOKEN" || "$TEACHER_TOKEN" == "null" ]]; then
  echo "Unable to obtain teacher token" >&2
  exit 1
fi

echo "=== Running OrderController-only Postman suite ==="
"${NEWMAN_BIN[@]}" run "$COLLECTION_FILE" -e "$ENV_FILE" \
  "${BASE_ENV_VARS[@]}" \
  --env-var "studentAccessToken=$STUDENT_TOKEN" \
  --env-var "teacherAccessToken=$TEACHER_TOKEN"

echo "\nOrderController Postman suite completed."
