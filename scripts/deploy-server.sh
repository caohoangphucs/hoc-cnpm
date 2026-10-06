#!/usr/bin/env bash
# Deploy tested images with Compose and apply the reviewed database migrations.
set -Eeuo pipefail
umask 077
sha=${1:?Commit SHA required}
server_image=${2:?Verified server image required}
client_image=${3:?Verified client image required}
[[ "$sha" =~ ^[a-f0-9]{40}$ ]]
[[ "$server_image" = "cnpm-server-ci:$sha" && "$client_image" = "cnpm-client-ci:$sha" ]]
export DEPLOY_DIR=${CNPM_DEPLOY_DIR:-/home/phuc/deployments/hoc-cnpm}
export WEB_PORT=${CNPM_WEB_PORT:-8091}
export DEPLOY_NETWORK=${CNPM_NETWORK:-cnpm-production}
database_container=${CNPM_DATABASE_CONTAINER:-cnpm-postgres}
database_user=${CNPM_DATABASE_USER:-cnpm_user}
database_name=${CNPM_DATABASE_NAME:-cnpm_db}
test -r "$DEPLOY_DIR/runtime.env"
# Catch missing production encryption configuration before replacing a working release.
awk -F= '$1 == "TOTP_ENCRYPTION_KEY" { count++; key_length = length($2) }
  END { if (count != 1 || key_length < 32) exit 1 }' "$DEPLOY_DIR/runtime.env" || {
  echo 'TOTP_ENCRYPTION_KEY must be set once and contain at least 32 characters in runtime.env' >&2
  exit 1
}
# Khóa AES-256 cho khách thăm phải tồn tại ổn định qua mọi lần deploy.
# Chỉ kiểm tra định dạng; tuyệt đối không in giá trị khóa vào log CI.
awk '$0 ~ /^KTX_PERSONAL_DATA_KEY=/ {
  count++; value=substr($0,23)
  if (length(value)==44 && value ~ /^[A-Za-z0-9+\/]+=$/) valid++
} END { if (count != 1 || valid != 1) exit 1 }' "$DEPLOY_DIR/runtime.env" || {
  echo 'KTX_PERSONAL_DATA_KEY must be a persistent Base64-encoded 32-byte key in runtime.env' >&2
  exit 1
}
exec 9> "$DEPLOY_DIR/deploy.lock"
flock -n 9 || { echo 'Another deployment is active'; exit 1; }
for image in "$server_image" "$client_image"; do
  [[ "$(docker image inspect "$image" --format '{{index .Config.Labels "cnpm.commit"}}')" = "$sha" ]]
done
repo_dir=$(cd "$(dirname "$0")/.." && pwd)
compose_file="$repo_dir/docker-compose.prod.yml"
release="$DEPLOY_DIR/release.env"
previous="$DEPLOY_DIR/previous-release.env"
# The initial adoption is performed once; subsequent releases only recreate apps.
test -r "$release"
cp "$release" "$previous"
compose() {
  docker compose -p cnpm --env-file "$1" -f "$compose_file" up -d --no-build --pull never
}
rollback() {
  code=$?
  trap - ERR
  echo 'Deployment failed; restoring previous images'
  cp "$previous" "$release"
  compose "$release" || { echo 'Rollback failed; inspect Compose logs'; exit 1; }
  exit "$code"
}
trap rollback ERR
# Apply non-destructive database migrations before starting code that uses them.
for migration in \
  V20261003__add_totp_secret.sql \
  V20261004__service_runtime_schema.sql \
  V20261005__repair_room_type_utf8.sql \
  V20261006__repair_vietnamese_display_data.sql \
  V20261007__backfill_demo_community_activities.sql \
  V20261008__repair_legacy_demo_passwords.sql \
  V20261009__complete_demo_usecases.sql \
  V20261010__evidence_file_content.sql \
  V20261011__seed_unissued_invoice_month.sql \
  V20261012__repair_seed_gate_violation_links.sql \
  V20261013__discipline_decision_text.sql \
  V20261014__support_invoice_meter_quantity.sql \
  V20261015__link_seed_residencies.sql; do
  echo "Applying database migration $migration"
  docker exec "$database_container" pg_isready -U "$database_user" -d "$database_name"
  docker exec -i -e PGCLIENTENCODING=UTF8 "$database_container" psql -X -v ON_ERROR_STOP=1 --single-transaction \
    -U "$database_user" -d "$database_name" \
    < "$repo_dir/database/migrations/$migration"
done
printf 'SERVER_IMAGE=%s\nCLIENT_IMAGE=%s\n' "$server_image" "$client_image" > "$release.tmp"
mv "$release.tmp" "$release"
compose "$release"
ready=false
health_timeout=${CNPM_HEALTH_TIMEOUT_SECONDS:-90}
[[ "$health_timeout" =~ ^[0-9]+$ ]] && (( health_timeout > 0 ))
deadline=$((SECONDS + health_timeout))
next_log=$((SECONDS + 10))
attempt=0
echo "Waiting up to ${health_timeout}s for application and database readiness"
while (( SECONDS < deadline )); do
  attempt=$((attempt + 1))
  if curl --fail --silent --max-time 5 "http://127.0.0.1:$WEB_PORT/api/health" | grep -q '"status":"UP"'; then
    ready=true; break
  fi
  container_state=$(docker inspect --format '{{.State.Status}}' cnpm-server 2>/dev/null || true)
  if [[ -n "$container_state" && "$container_state" != running ]]; then
    echo "Backend container is $container_state; recent logs follow" >&2
    docker logs --tail 80 cnpm-server >&2 || true
    break
  fi
  if (( SECONDS >= next_log )); then
    echo "Still waiting for readiness (attempt $attempt; backend state: ${container_state:-unknown})"
    next_log=$((SECONDS + 10))
  fi
  sleep 2
done
if [[ "$ready" != true ]]; then
  echo 'Application did not become ready; recent backend logs follow' >&2
  docker logs --tail 80 cnpm-server >&2 || true
fi
[[ "$ready" = true ]]
curl --fail --silent --max-time 5 "http://127.0.0.1:$WEB_PORT/" | grep -q 'KTX Smart Hub'
printf '%s\n' "$sha" > "$DEPLOY_DIR/deployed-sha"
trap - ERR
echo "Deployed $sha with Docker Compose"
