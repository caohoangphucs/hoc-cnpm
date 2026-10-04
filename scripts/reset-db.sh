#!/usr/bin/env bash
# Reset only local/demo PostgreSQL. This deletes existing demo data.
set -euo pipefail
[[ "${1:-}" = --reset-demo ]] || { echo 'Usage: bash scripts/reset-db.sh --reset-demo'; exit 1; }
cd "$(dirname "$0")/.."
docker compose up -d --wait cnpm-db
for file in database/01_schema.sql database/02_seed.sql database/04_demo_usecases.sql; do
  docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 --single-transaction' < "$file"
done
printf '%s\n' "SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';" |
  docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
echo 'PostgreSQL demo reset completed.'
