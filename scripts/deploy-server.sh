#!/usr/bin/env bash
# Deploy locally tested images with Compose; never alter production DB data.
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
test -r "$DEPLOY_DIR/runtime.env"
exec 9> "$DEPLOY_DIR/deploy.lock"
flock -n 9 || { echo 'Another deployment is active'; exit 1; }
for image in "$server_image" "$client_image"; do
  [[ "$(docker image inspect "$image" --format '{{index .Config.Labels "cnpm.commit"}}')" = "$sha" ]]
done
compose_file=$(cd "$(dirname "$0")/.." && pwd)/docker-compose.prod.yml
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
printf 'SERVER_IMAGE=%s\nCLIENT_IMAGE=%s\n' "$server_image" "$client_image" > "$release.tmp"
mv "$release.tmp" "$release"
compose "$release"
ready=false
for attempt in $(seq 1 "${CNPM_HEALTH_ATTEMPTS:-60}"); do
  if curl --fail --silent --max-time 5 "http://127.0.0.1:$WEB_PORT/api/health" | grep -q '"status":"UP"'; then
    ready=true; break
  fi
  sleep 2
done
[[ "$ready" = true ]]
curl --fail --silent --max-time 5 "http://127.0.0.1:$WEB_PORT/" | grep -q 'KTX Smart Hub'
printf '%s\n' "$sha" > "$DEPLOY_DIR/deployed-sha"
trap - ERR
echo "Deployed $sha with Docker Compose"
