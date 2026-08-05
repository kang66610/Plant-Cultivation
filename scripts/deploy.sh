#!/usr/bin/env bash
set -euo pipefail

BASE=/www/wwwroot/plant-cultivation
FRONTEND=$BASE/frontend
BACKEND=$BASE/backend
LOG=$BASE/deploy.log
SERVICE=${SERVICE:-plant-cultivation}
HEALTH_URL=${HEALTH_URL:-http://127.0.0.1:8080/api/health}

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG"; }

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "ERROR: missing command: $1"
    exit 1
  fi
}

service_exists() {
  command -v systemctl >/dev/null 2>&1 && systemctl list-unit-files 2>/dev/null | grep -q "^$SERVICE.service"
}

resolve_frontend_source() {
  local candidate="$1"
  if [ -f "$candidate/index.html" ]; then
    echo "$candidate"
    return 0
  fi
  if [ -f "$candidate/dist/index.html" ]; then
    echo "$candidate/dist"
    return 0
  fi
  return 1
}

rollback() {
  log "Rolling back to previous version..."
  if [ -f "$BACKEND/app.jar.bak" ]; then
    mv -f "$BACKEND/app.jar.bak" "$BACKEND/app.jar"
  fi
  if [ -d "$FRONTEND/dist.bak" ]; then
    rm -rf "$FRONTEND/dist"
    mv "$FRONTEND/dist.bak" "$FRONTEND/dist"
  fi
  if service_exists; then
    systemctl restart "$SERVICE" || true
  else
    PID=$(pgrep -f "app.jar" || true)
    [ -n "$PID" ] && kill "$PID" || true
    cd "$BACKEND"
    nohup java -jar app.jar > "$BACKEND/app.log" 2>&1 &
  fi
}

show_backend_logs() {
  systemctl status "$SERVICE" --no-pager -l || true
  journalctl -u "$SERVICE" -n 50 --no-pager || true
  [ -f "$BACKEND/app.log" ] && tail -n 80 "$BACKEND/app.log" || true
}

configure_systemd_env() {
  if [ -f "$BASE/.env" ] && command -v systemctl >/dev/null 2>&1; then
    if ! mkdir -p "/etc/systemd/system/$SERVICE.service.d" 2>/dev/null; then
      log "WARN: cannot write systemd drop-in; make sure $SERVICE.service reads $BASE/.env"
      return
    fi
    if ! cat > "/etc/systemd/system/$SERVICE.service.d/env.conf" <<EOF
[Service]
EnvironmentFile=$BASE/.env
EOF
    then
      log "WARN: cannot write systemd env file; make sure $SERVICE.service reads $BASE/.env"
      return
    fi
    systemctl daemon-reload || log "WARN: systemctl daemon-reload failed"
  fi
}

require_cmd curl

if [ ! -d "$BASE" ] || [ ! -d "$FRONTEND" ] || [ ! -d "$BACKEND" ]; then
  log "ERROR: project directories are incomplete under $BASE"
  exit 1
fi

if [ ! -s "$BACKEND/app.jar.new" ]; then
  log "ERROR: $BACKEND/app.jar.new does not exist"
  exit 1
fi

NEW_FRONTEND=""
if [ -d "$FRONTEND/frontend-new" ]; then
  NEW_FRONTEND="$FRONTEND/frontend-new"
elif [ -d "$BASE/frontend-new" ]; then
  NEW_FRONTEND="$BASE/frontend-new"
fi

if [ -n "$NEW_FRONTEND" ]; then
  if ! RESOLVED_FRONTEND="$(resolve_frontend_source "$NEW_FRONTEND")"; then
    log "ERROR: $NEW_FRONTEND does not contain index.html or dist/index.html"
    exit 1
  fi
  log "Switching frontend dist..."
  rm -rf "$FRONTEND/dist.bak"
  [ -d "$FRONTEND/dist" ] && mv "$FRONTEND/dist" "$FRONTEND/dist.bak"
  mv "$RESOLVED_FRONTEND" "$FRONTEND/dist"
  if [ "$RESOLVED_FRONTEND" != "$NEW_FRONTEND" ]; then
    rm -rf "$NEW_FRONTEND"
  fi
  chmod -R 755 "$FRONTEND/dist" || true
else
  log "WARN: frontend-new does not exist; keeping current frontend"
fi

log "Switching backend jar..."
rm -f "$BACKEND/app.jar.bak"
[ -f "$BACKEND/app.jar" ] && mv "$BACKEND/app.jar" "$BACKEND/app.jar.bak"
mv "$BACKEND/app.jar.new" "$BACKEND/app.jar"

if [ -f "$BASE/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "$BASE/.env"
  set +a
else
  log "WARN: $BASE/.env does not exist"
fi

SQL_FILE=""
if [ -f "$BASE/scripts/fill_missing_care_guides.sql" ]; then
  SQL_FILE="$BASE/scripts/fill_missing_care_guides.sql"
elif [ -f "$BASE/fill_missing_care_guides.sql" ]; then
  SQL_FILE="$BASE/fill_missing_care_guides.sql"
fi

if [ -n "$SQL_FILE" ] && [ "${RUN_DB_PATCHES:-1}" = "1" ]; then
  if command -v mysql >/dev/null 2>&1 && [ -n "${DB_PASSWORD:-}" ]; then
    DB_NAME=${DB_NAME:-plant_cultivation}
    DB_USER=${DB_USERNAME:-plant_cultivation}
    DB_HOST=${DB_HOST:-localhost}
    log "Applying database patch: $SQL_FILE"
    mysql --default-character-set=utf8mb4 -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SQL_FILE" \
      || log "WARN: database patch failed; please run $SQL_FILE manually"
  else
    log "WARN: skip database patch; mysql command or DB_PASSWORD is missing"
  fi
fi

if service_exists; then
  configure_systemd_env
  log "Restarting systemd service $SERVICE..."
  if ! systemctl restart "$SERVICE"; then
    log "ERROR: systemd restart failed"
    show_backend_logs
    rollback
    exit 1
  fi
  if ! systemctl is-active --quiet "$SERVICE"; then
    log "ERROR: service is not active after restart"
    show_backend_logs
    rollback
    exit 1
  fi
else
  log "systemd service not found; starting with nohup..."
  PID=$(pgrep -f "app.jar" || true)
  if [ -n "$PID" ]; then
    kill "$PID"
    sleep 2
  fi
  cd "$BACKEND"
  nohup java -jar app.jar > "$BACKEND/app.log" 2>&1 &
fi

log "Waiting for backend health..."
BACKEND_OK=0
for i in $(seq 1 15); do
  if curl -sf -o /dev/null "$HEALTH_URL"; then
    BACKEND_OK=1
    break
  fi
  sleep 2
done

if [ "$BACKEND_OK" = "1" ]; then
  log "Health check passed"
else
  log "ERROR: backend did not become healthy in 30 seconds"
  show_backend_logs
  rollback
  exit 1
fi

if command -v nginx >/dev/null 2>&1; then
  if nginx -t >/dev/null 2>&1; then
    nginx -s reload || log "WARN: nginx reload failed"
  else
    log "WARN: nginx config test failed; skipped reload"
  fi
fi

log "Deployment complete"
