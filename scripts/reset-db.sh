#!/usr/bin/env bash
# Reset only local/demo PostgreSQL. This deletes existing demo data.
set -euo pipefail
[[ "${1:-}" = --reset-demo ]] || { echo 'Usage: bash scripts/reset-db.sh --reset-demo'; exit 1; }
cd "$(dirname "$0")/.."
docker compose up -d --wait cnpm-db
# Áp dụng schema rồi seed theo thứ tự phụ thuộc; mỗi file chạy trong một transaction riêng.
for file in database/01_schema.sql database/02_seed.sql database/04_demo_usecases.sql database/05_evidence_file_content.sql database/06_invoice_meter_demo.sql database/07_gate_violation_demo.sql database/08_residency_links.sql; do
  docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1 --single-transaction -f "$1"' sh "/docker-entrypoint-initdb.d/${file#database/}"
done
printf '%s\n' "SELECT COUNT(*) AS tables FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';" |
  docker compose exec -T cnpm-db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1'
echo 'PostgreSQL demo reset completed.'
