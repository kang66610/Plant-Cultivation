#!/usr/bin/env bash
set -euo pipefail

BASE=/www/wwwroot/plant-cultivation
FRONTEND=$BASE/frontend
BACKEND=$BASE/backend
LOG=$BASE/deploy.log
HEALTH_URL=http://127.0.0.1:8080/api/health

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG"; }

rollback() {
  log "Rolling back to previous version..."
  if [ -f "$BACKEND/app.jar.bak" ]; then
    mv -f "$BACKEND/app.jar.bak" "$BACKEND/app.jar"
  fi
  if [ -d "$FRONTEND/dist.bak" ]; then
    rm -rf "$FRONTEND/dist"
    mv "$FRONTEND/dist.bak" "$FRONTEND/dist"
  fi
  if systemctl list-units --type=service 2>/dev/null | grep -q "plant-cultivation"; then
    systemctl restart plant-cultivation || true
  else
    PID=$(pgrep -f "app.jar" || true)
    [ -n "$PID" ] && kill "$PID" || true
    cd "$BACKEND"
    nohup java -jar app.jar > "$BACKEND/app.log" 2>&1 &
  fi
}

show_backend_logs() {
  systemctl status plant-cultivation --no-pager -l || true
  journalctl -u plant-cultivation -n 50 --no-pager || true
  [ -f "$BACKEND/app.log" ] && tail -n 80 "$BACKEND/app.log" || true
}

configure_systemd_env() {
  if [ -f "$BASE/.env" ] && command -v systemctl >/dev/null 2>&1; then
    if ! mkdir -p /etc/systemd/system/plant-cultivation.service.d 2>/dev/null; then
      log "WARN: cannot write systemd drop-in; make sure plant-cultivation.service reads $BASE/.env"
      return
    fi
    if ! cat > /etc/systemd/system/plant-cultivation.service.d/env.conf <<EOF
[Service]
EnvironmentFile=$BASE/.env
EOF
    then
      log "WARN: cannot write systemd env file; make sure plant-cultivation.service reads $BASE/.env"
      return
    fi
    systemctl daemon-reload || log "WARN: systemctl daemon-reload failed"
  fi
}

if [ ! -f "$BACKEND/app.jar.new" ]; then
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
  log "Switching frontend dist..."
  rm -rf "$FRONTEND/dist.bak"
  [ -d "$FRONTEND/dist" ] && mv "$FRONTEND/dist" "$FRONTEND/dist.bak"
  mv "$NEW_FRONTEND" "$FRONTEND/dist"
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

if systemctl list-units --type=service 2>/dev/null | grep -q "plant-cultivation"; then
  configure_systemd_env
  log "Restarting systemd service plant-cultivation..."
  if ! systemctl restart plant-cultivation; then
    log "ERROR: systemd restart failed"
    show_backend_logs
    rollback
    exit 1
  fi
  if ! systemctl is-active --quiet plant-cultivation; then
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

log "Deployment complete"
