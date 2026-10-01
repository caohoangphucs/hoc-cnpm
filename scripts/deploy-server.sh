#!/usr/bin/env bash
# Trusted develop-only job. No schema/seed replay on an initialized database.
set -euo pipefail
umask 077
sha=${1:?Commit SHA required}
image=${2:?Verified local image required}
[[ "$sha" =~ ^[a-f0-9]{40}$ ]]
deploy_dir=/home/phuc/deployments/hoc-cnpm
test -r "$deploy_dir/runtime.env"
exec 9> "$deploy_dir/deploy.lock"
flock -n 9 || { echo 'Another deployment is active'; exit 1; }
# Check readiness before stopping the current app.
docker exec cnpm-postgres pg_isready -U cnpm_user -d cnpm_db
schema_count=$(docker exec cnpm-postgres psql -U cnpm_user -d cnpm_db -Atc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")
[[ "$schema_count" = 27 ]] || { echo 'Unexpected schema; manual review required'; exit 1; }
[[ "$image" = "cnpm-ci:$sha" ]]
[[ "$(docker image inspect "$image" --format '{{index .Config.Labels "cnpm.commit"}}')" = "$sha" ]]
docker tag "$image" "cnpm-release:$sha"
previous="cnpm-web-previous-${sha:0:12}"
# Do not discard an existing rollback container.
if docker inspect "$previous" >/dev/null 2>&1; then echo 'Rollback container already exists; review before retry'; exit 1; fi
rollback() {
  code=$?
  trap - ERR
  docker logs --tail 80 cnpm-web 2>&1 || true
  if docker inspect "$previous" >/dev/null 2>&1; then
    docker rm -f cnpm-web >/dev/null 2>&1 || true
    docker rename "$previous" cnpm-web
    docker start cnpm-web
  elif docker inspect cnpm-web >/dev/null 2>&1; then
    docker start cnpm-web
  fi
  echo 'Deployment failed; previous app restored if available'
  exit "$code"
}
trap rollback ERR
if docker inspect cnpm-web >/dev/null 2>&1; then
  docker stop cnpm-web
  docker rename cnpm-web "$previous"
fi
docker run -d --name cnpm-web --restart unless-stopped \
  --network cnpm-production -p 8091:8080 \
  --env-file "$deploy_dir/runtime.env" \
  --label "cnpm.commit=$sha" "cnpm-release:$sha"
ready=false
for attempt in $(seq 1 60); do
  if curl --fail --silent http://127.0.0.1:8091/ > "$deploy_dir/index-check.html"; then ready=true; break; fi
  sleep 2
done
[[ "$ready" = true ]]
# A DB-backed request catches pool/function errors hidden by the landing page.
curl --fail --silent --show-error \
  --data-urlencode username=ci-nonexistent-account --data-urlencode password=invalid \
  http://127.0.0.1:8091/auth/login > "$deploy_dir/login-check.html"
grep -q 'không chính xác' "$deploy_dir/login-check.html"
if docker logs cnpm-web 2>&1 | grep -E 'SEVERE|Exception|ERROR'; then false; fi
printf '%s\n' "$sha" > "$deploy_dir/deployed-sha"
trap - ERR
echo "Deployed $sha; rollback container: $previous"
