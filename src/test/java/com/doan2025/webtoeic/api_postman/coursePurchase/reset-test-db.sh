#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="${SEED_FILE:-$SCRIPT_DIR/Sample_data.sql/sample_test_data_payment.sql}"
SCHEMA_FILE="${SCHEMA_FILE:-$SCRIPT_DIR/BE-develop/doan21.sql}"
DB_NAME="${DB_NAME:-doand21}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3307}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-An198205}"
ACTION="${1:-reset}"

if [[ "$ACTION" == "help" || "$ACTION" == "-h" || "$ACTION" == "--help" ]]; then
  cat <<EOF
Usage: $(basename "$0") [reset|seed|cleanup]

Defaults:
  ACTION=${ACTION}
  DB_NAME=${DB_NAME}
  SEED_FILE=${SEED_FILE}

This script targets the local web database used by the exercise by default.
If your backend points to another schema, override DB_NAME explicitly.

Environment overrides:
  MYSQL_HOST, MYSQL_PORT, MYSQL_USER, MYSQL_PASSWORD, DB_NAME, SEED_FILE

Actions:
  reset    Drop/recreate the test database and import the seed SQL (default)
  seed     Create the database if needed and import the seed SQL
  cleanup  Drop and recreate the database without importing seed data
EOF
  exit 0
fi

if ! command -v mysql >/dev/null 2>&1; then
  if [[ -x "/usr/local/opt/mysql-client/bin/mysql" ]]; then
    export PATH="/usr/local/opt/mysql-client/bin:$PATH"
  elif command -v brew >/dev/null 2>&1; then
    MYSQL_CLIENT_PREFIX="$(brew --prefix mysql-client 2>/dev/null || true)"
    if [[ -n "$MYSQL_CLIENT_PREFIX" && -x "$MYSQL_CLIENT_PREFIX/bin/mysql" ]]; then
      export PATH="$MYSQL_CLIENT_PREFIX/bin:$PATH"
    fi
  fi
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "mysql client is required but was not found in PATH" >&2
  exit 1
fi

if [[ ! -f "$SEED_FILE" ]]; then
  echo "Seed file not found: $SEED_FILE" >&2
  exit 1
fi

if [[ ! -f "$SCHEMA_FILE" ]]; then
  echo "Schema file not found: $SCHEMA_FILE" >&2
  exit 1
fi

MYSQL_BASE=(mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER")
if [[ -n "$MYSQL_PASSWORD" ]]; then
  MYSQL_BASE+=("-p$MYSQL_PASSWORD")
fi

run_mysql() {
  "${MYSQL_BASE[@]}" "$@"
}

drop_and_create_database() {
  run_mysql -e "DROP DATABASE IF EXISTS \`$DB_NAME\`; CREATE DATABASE \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}

import_seed() {
  local temp_sql
  temp_sql="$(mktemp)"
  trap 'rm -f "$temp_sql"' RETURN

  sed "s/^USE doand21;/USE ${DB_NAME};/" "$SEED_FILE" > "$temp_sql"
  run_mysql "$DB_NAME" < "$temp_sql"
}

import_schema() {
  run_mysql "$DB_NAME" < "$SCHEMA_FILE"
}

case "$ACTION" in
  reset)
    echo "Resetting database '$DB_NAME' from '$SEED_FILE'..."
    drop_and_create_database
    import_schema
    import_seed
    echo "Done: database '$DB_NAME' is clean and seeded."
    ;;
  cleanup)
    echo "Cleaning database '$DB_NAME'..."
    drop_and_create_database
    echo "Done: database '$DB_NAME' was dropped and recreated."
    ;;
  seed)
    echo "Seeding database '$DB_NAME' from '$SEED_FILE'..."
    run_mysql -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    import_seed
    echo "Done: database '$DB_NAME' was seeded."
    ;;
  *)
    echo "Unknown action: $ACTION" >&2
    exit 1
    ;;
esac