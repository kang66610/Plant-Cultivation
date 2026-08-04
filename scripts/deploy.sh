#!/usr/bin/env bash
# ============================================================
# 服务器端部署脚本
# 由 GitHub Actions 通过 SSH 调用，执行前端/后端原子切换
#
# 目录结构：
#   /www/wwwroot/plant-cultivation/
#   ├── .env                  # 生产环境变量（数据库密码、JWT 密钥等）
#   ├── deploy.sh             # 本脚本
#   ├── backend/
#   │   ├── app.jar           # 当前运行版本
#   │   └── app.jar.new       # GitHub Actions 上传的新版本
#   └── frontend/
#       ├── dist/             # 当前运行版本（Nginx 站点目录）
#       └── frontend-new/     # GitHub Actions 上传的新版本
#
# 用法：bash /www/wwwroot/plant-cultivation/deploy.sh
# ============================================================
set -euo pipefail

BASE=/www/wwwroot/plant-cultivation
FRONTEND=$BASE/frontend
BACKEND=$BASE/backend
LOG=$BASE/deploy.log

log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "$LOG"; }

# ---------- 1. 检查上传是否完整 ----------
if [ ! -f "$BACKEND/app.jar.new" ]; then
  log "ERROR: $BACKEND/app.jar.new 不存在，跳过后端更新"
  exit 1
fi

# ---------- 2. 前端原子切换 ----------
if [ -d "$FRONTEND/frontend-new" ]; then
  log "切换前端目录..."
  rm -rf "$FRONTEND/dist.bak"
  [ -d "$FRONTEND/dist" ] && mv "$FRONTEND/dist" "$FRONTEND/dist.bak"
  mv "$FRONTEND/frontend-new" "$FRONTEND/dist"
  rm -rf "$FRONTEND/dist.bak"
else
  log "WARN: frontend-new 不存在，跳过前端更新"
fi

# ---------- 3. 替换后端 jar ----------
log "替换后端 jar..."
rm -f "$BACKEND/app.jar.bak"
[ -f "$BACKEND/app.jar" ] && mv "$BACKEND/app.jar" "$BACKEND/app.jar.bak"
mv "$BACKEND/app.jar.new" "$BACKEND/app.jar"

# ---------- 4. 加载生产环境变量 ----------
if [ -f "$BASE/.env" ]; then
  set -a
  # shellcheck disable=SC1091
  source "$BASE/.env"
  set +a
else
  log "WARN: $BASE/.env 不存在，使用默认配置"
fi

# ---------- 5. 重启后端服务 ----------
# 方式 A：systemd 服务（推荐，宝塔 Java 项目管理器也是这种方式）
if systemctl list-units --type=service 2>/dev/null | grep -q "plant-cultivation"; then
  log "重启 systemd 服务 plant-cultivation..."
  systemctl restart plant-cultivation
  systemctl is-active plant-cultivation
# 方式 B：无 systemd 时用 nohup 后台运行
else
  log "未找到 systemd 服务，使用 nohup 方式重启..."
  PID=$(pgrep -f "app.jar" || true)
  if [ -n "$PID" ]; then
    kill "$PID"
    sleep 2
  fi
  cd "$BACKEND"
  nohup java -jar app.jar > "$BACKEND/app.log" 2>&1 &
fi

log "部署完成 ✔"
