#!/usr/bin/env bash
# Deploy the two verified images together. Never replay schema/seed on production.
set -Eeuo pipefail
umask 077
sha=${1:?Commit SHA required}
server_image=${2:?Verified server image required}
client_image=${3:?Verified client image required}
[[ "$sha" =~ ^[a-f0-9]{40}$ ]]
[[ "$server_image" = "cnpm-server-ci:$sha" && "$client_image" = "cnpm-client-ci:$sha" ]]
deploy_dir=${CNPM_DEPLOY_DIR:-/home/phuc/deployments/hoc-cnpm}
web=${CNPM_WEB_CONTAINER:-cnpm-web}
server=${CNPM_SERVER_CONTAINER:-cnpm-server}
database=${CNPM_DB_CONTAINER:-cnpm-postgres}
network=${CNPM_NETWORK:-cnpm-production}
port=${CNPM_WEB_PORT:-8091}
test -r "$deploy_dir/runtime.env"
exec 9> "$deploy_dir/deploy.lock"
flock -n 9 || { echo 'Another deployment is active'; exit 1; }
for image in "$server_image" "$client_image"; do
  [[ "$(docker image inspect "$image" --format '{{index .Config.Labels "cnpm.commit"}}')" = "$sha" ]]
done
docker exec "$database" pg_isready -U cnpm_user -d cnpm_db
count=$(docker exec "$database" psql -U cnpm_user -d cnpm_db -Atc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")
[[ "$count" = 27 ]] || { echo 'Schema requires manual review'; exit 1; }
previous_web="$web-previous-${sha:0:12}"
previous_server="$server-previous-${sha:0:12}"
for name in "$previous_web" "$previous_server"; do
  if docker inspect "$name" >/dev/null 2>&1; then echo 'Rollback container exists; review before retry'; exit 1; fi
done
docker tag "$server_image" "cnpm-server-release:$sha"
docker tag "$client_image" "cnpm-client-release:$sha"
restore() {
  local name=$1 previous=$2
  if docker inspect "$previous" >/dev/null 2>&1; then
    docker rm -f "$name" >/dev/null 2>&1 || true
    docker rename "$previous" "$name"
    docker start "$name"
  elif docker inspect "$name" >/dev/null 2>&1; then
    if [[ "$(docker inspect "$name" --format '{{index .Config.Labels "cnpm.commit"}}')" = "$sha" ]]; then
      docker rm -f "$name" >/dev/null
    else
      docker start "$name"
    fi
  fi
}
rollback() {
  code=$?
  trap - ERR
  docker logs --tail 60 "$server" 2>&1 || true
  docker logs --tail 30 "$web" 2>&1 || true
  restore "$server" "$previous_server"
  restore "$web" "$previous_web"
  echo 'Deployment failed; previous backend/frontend restored'
  exit "$code"
}
trap rollback ERR
for name in "$web" "$server"; do
  if docker inspect "$name" >/dev/null 2>&1; then
    docker stop "$name"
    docker rename "$name" "$name-previous-${sha:0:12}"
  fi
done
docker run -d --name "$server" --restart unless-stopped --network "$network" \
  --network-alias cnpm-server --env-file "$deploy_dir/runtime.env" \
  -e SPRING_PROFILES_ACTIVE=prod --label "cnpm.commit=$sha" "cnpm-server-release:$sha"
docker run -d --name "$web" --restart unless-stopped --network "$network" \
  -p "$port:80" --label "cnpm.commit=$sha" "cnpm-client-release:$sha"
ready=false
for attempt in $(seq 1 "${CNPM_HEALTH_ATTEMPTS:-60}"); do
  if curl --fail --silent "http://127.0.0.1:$port/api/health" > "$deploy_dir/health-check.json" \
      && grep -q '"status":"UP"' "$deploy_dir/health-check.json"; then ready=true; break; fi
  sleep 2
done
[[ "$ready" = true ]]
curl --fail --silent "http://127.0.0.1:$port/" > "$deploy_dir/index-check.html"
grep -q 'KTX Smart Hub' "$deploy_dir/index-check.html"
if docker logs "$server" 2>&1 | grep -E 'ERROR|APPLICATION FAILED TO START'; then false; fi
printf '%s\n' "$sha" > "$deploy_dir/deployed-sha"
trap - ERR
echo "Deployed backend/frontend $sha; rollback: $previous_server / $previous_web"
