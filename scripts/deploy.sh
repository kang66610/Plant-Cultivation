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
# 清理历史遗留的旧上传路径（2026-08 前 CI 误传至 $BASE/frontend-new）
[ -d "$BASE/frontend-new" ] && rm -rf "$BASE/frontend-new"
if [ -d "$FRONTEND/frontend-new" ]; then
  log "切换前端目录..."
  # 清理上一次的备份（本次的 dist.bak 保留为上一版本，供回滚）
  rm -rf "$FRONTEND/dist.bak"
  [ -d "$FRONTEND/dist" ] && mv "$FRONTEND/dist" "$FRONTEND/dist.bak"
  mv "$FRONTEND/frontend-new" "$FRONTEND/dist"
else
  log "WARN: frontend-new 不存在，跳过前端更新"
fi

# ---------- 3. 替换后端 jar ----------
log "替换后端 jar..."
# 清理上一次的备份（本次的 app.jar.bak 保留为上一版本，供回滚）
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

# ---------- 6. 健康检查 ----------
log "等待后端就绪（最多 30 秒）..."
BACKEND_OK=0
for i in $(seq 1 15); do
  if curl -sf -o /dev/null "http://127.0.0.1:8080/api/plants?page=1&size=1"; then
    BACKEND_OK=1
    break
  fi
  sleep 2
done
if [ "$BACKEND_OK" = "1" ]; then
  log "健康检查通过 ✔"
else
  log "ERROR: 后端 30 秒内未就绪！可回滚上一版本："
  log "  mv $BACKEND/app.jar.bak $BACKEND/app.jar && systemctl restart plant-cultivation"
  log "  （前端回滚：rm -rf $FRONTEND/dist && mv $FRONTEND/dist.bak $FRONTEND/dist）"
fi

log "部署完成 ✔"
