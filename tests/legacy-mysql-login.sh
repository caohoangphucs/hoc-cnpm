#!/usr/bin/env bash
set -euo pipefail
mysql_args=(--host=127.0.0.1 --port="${MYSQL_TEST_PORT:-33060}" --user=root --default-character-set=utf8mb4 --batch --skip-column-names)
mysql "${mysql_args[@]}" < tests/fixtures/legacy-mysql-login.sql
error_file=$(mktemp)
trap 'rm -f "$error_file"' EXIT
if mysql "${mysql_args[@]}" -e "CALL cnpm_db.usp_DangNhap('admin')" 2> "$error_file"; then
  echo 'Expected original collation mismatch was not reproduced'
  exit 1
fi
grep -q 'Illegal mix of collations' "$error_file"
mysql "${mysql_args[@]}" < scripts/sql/repair-legacy-mysql-login.sql
test "$(mysql "${mysql_args[@]}" -e "CALL cnpm_db.usp_DangNhap('admin')" | cut -f1)" = 1
test "$(mysql "${mysql_args[@]}" -e "CALL cnpm_db.usp_DangNhap('sinhviên')" | cut -f1)" = 2
test -z "$(mysql "${mysql_args[@]}" -e "CALL cnpm_db.usp_DangNhap('missing-ci-account')")"
test "$(mysql "${mysql_args[@]}" -e "SELECT COUNT(*) FROM cnpm_db.TAI_KHOAN")" = 2
# Reapplying the manual repair is safe and preserves its result.
mysql "${mysql_args[@]}" < scripts/sql/repair-legacy-mysql-login.sql
test "$(mysql "${mysql_args[@]}" -e "CALL cnpm_db.usp_DangNhap('admin')" | cut -f1)" = 1
echo 'Legacy MySQL collation regression PASS'
