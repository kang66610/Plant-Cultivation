# DEPLOYMENT.md — 部署与运维指南

> 本项目的部署已完全自动化（GitHub Actions + systemd），日常更新只需 `git push`。

---

## 一、日常部署（90% 的场景）

```bash
git add .
git commit -m "本次修改说明"
git push origin master
```

**流程**：GitHub Actions 云端构建 → SSH 上传服务器 → 自动切换文件 → 自动重启后端。

- 耗时约 3 分钟
- 查看状态：https://github.com/kang66610/Plant-Cultivation/actions
- 三个任务全绿 = 部署成功（Build Backend / Build Frontend / Deploy to Server）
- 手动触发部署：Actions 页面 → Run workflow

---

## 二、架构总览

```
用户浏览器
    │ http://175.178.200.181:80
    ▼
Nginx（站点 175.178.200.181）
    ├── / → /www/wwwroot/plant-cultivation/frontend/dist   （前端静态文件）
    ├── /api/ → 反向代理 127.0.0.1:8080                    （后端接口）
    └── /uploads/ → 反向代理 127.0.0.1:8080                （图片上传）
    ▼
Spring Boot 后端（systemd 服务 plant-cultivation）
    └── jar: /www/wwwroot/plant-cultivation/backend/app.jar
        ▼
MySQL 8.0（宝塔管理，数据库 plant_cultivation）
```

---

## 三、服务器信息

| 项目 | 值 |
|------|-----|
| 服务器 | 腾讯云 175.178.200.181（CentOS，宝塔面板） |
| SSH 登录 | `ssh root@175.178.200.181` |
| JDK | `/www/server/java/jdk-17.0.8/bin/java`（宝塔安装） |
| 项目根目录 | `/www/wwwroot/plant-cultivation/` |
| 后端 jar | `/www/wwwroot/plant-cultivation/backend/app.jar` |
| 前端目录 | `/www/wwwroot/plant-cultivation/frontend/dist/` |
| 环境变量 | `/www/wwwroot/plant-cultivation/.env` |
| 上传图片 | `/www/wwwroot/uploads/` |
| 部署日志 | `/www/wwwroot/plant-cultivation/deploy.log` |
| systemd 服务 | `plant-cultivation` |

---

## 四、常用运维命令（服务器上执行）

| 场景 | 命令 |
|------|------|
| 查看后端状态 | `systemctl status plant-cultivation` |
| 重启后端 | `systemctl restart plant-cultivation` |
| 实时看后端日志 | `journalctl -u plant-cultivation -f` |
| 最近 30 条日志 | `journalctl -u plant-cultivation -n 30` |
| Nginx 状态 | `/etc/init.d/nginx status` |
| 重载 Nginx | `/etc/init.d/nginx reload` |
| 部署日志 | `tail -f /www/wwwroot/plant-cultivation/deploy.log` |
| 查看端口 | `ss -tlnp \| grep -E "8080\|80"` |

---

## 五、CI/CD 配置说明

### GitHub Secrets（仓库 Settings → Secrets）

| Secret | 用途 |
|--------|------|
| `SERVER_HOST` | 服务器 IP：175.178.200.181 |
| `SERVER_USER` | root |
| `SERVER_PORT` | 22 |
| `SERVER_SSH_KEY` | 服务器私钥 `~/.ssh/github_actions`（**base64 编码后存储**，workflow 里 `base64 -d` 解码） |

### 关键文件

| 文件 | 作用 |
|------|------|
| `.github/workflows/deploy.yml` | 构建 + 部署流水线 |
| `scripts/deploy.sh` | 服务器端切换脚本（上传到 `/www/wwwroot/plant-cultivation/deploy.sh`） |
| `scripts/server/plant-cultivation.service` | systemd 服务模板 |
| `scripts/server/.env.example` | 环境变量模板 |

### 部署目录约定（deploy.sh 执行）

```
/www/wwwroot/plant-cultivation/
├── backend/app.jar.new     ← CI 上传的新 jar
├── frontend/frontend-new/  ← CI 上传的新前端
├── backend/app.jar         ← 切换后运行
├── frontend/dist/          ← 切换后提供访问
└── deploy.sh               ← 执行切换 + 重启
```

---

## 六、环境变量（.env）

文件位置：`/www/wwwroot/plant-cultivation/.env`（不要提交到 git）

```bash
DB_PASSWORD=你的数据库密码
JWT_SECRET=你的JWT密钥
UPLOAD_DIR=/www/wwwroot/uploads
```

修改后需重启服务生效：`systemctl restart plant-cultivation`

---

## 七、数据库

- 数据库：`plant_cultivation`（MySQL 8.0，宝塔管理）
- 备份：宝塔面板 → 计划任务 → 每天自动备份，保留 7 份
- ⚠️ **改表结构时**：需先在服务器执行 SQL 升级脚本，再 push 代码

---

## 八、故障排查

### 部署失败（Actions 红色）

1. 看 Deploy 步骤日志：SSH 连接失败？上传失败？deploy.sh 报错？
2. 服务器上查：`systemctl status plant-cultivation`、`tail -50 /www/wwwroot/plant-cultivation/deploy.log`

### 网页打不开

1. 手机流量访问 http://175.178.200.181 —— 能开则电脑浏览器问题（清缓存/代理）
2. 服务器上：`curl -I http://175.178.200.181` —— 异常则查 Nginx
3. 后端：`systemctl status plant-cultivation` + `journalctl -u plant-cultivation -n 30`

### 回滚（出问题时）

deploy.sh 切换前会保留备份，手动恢复：

```bash
# 后端回滚到上一个版本
mv /www/wwwroot/plant-cultivation/backend/app.jar.bak /www/wwwroot/plant-cultivation/backend/app.jar
systemctl restart plant-cultivation
```

---

*维护时间：2026-08-04 随 CI/CD 搭建完成创建，如有变动及时更新本文件。*
