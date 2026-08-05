# DEPLOYMENT.md - 部署与运维指南

> 生产环境使用宝塔 Nginx + systemd + MySQL。日常更新可通过 GitHub Actions 自动部署，也可以手动上传 `app.jar.new` 与 `frontend-new` 后执行 `deploy.sh`。

---

## 一、日常部署

```bash
git add .
git commit -m "本次修改说明"
git push origin master
```

流程：GitHub Actions 云端构建 -> SSH 上传服务器 -> `deploy.sh` 切换文件 -> 执行可选数据库补丁 -> 重启后端 -> 健康检查。

- 查看状态：https://github.com/kang66610/Plant-Cultivation/actions
- 三个任务全绿 = 部署成功：Build Backend / Build Frontend / Deploy to Server
- 手动触发部署：Actions 页面 -> Run workflow

---

## 二、架构总览

```text
用户浏览器
    | http://175.178.200.181:80 / https://域名:443
    v
Nginx（宝塔站点）
    |-- /        -> /www/wwwroot/plant-cultivation/frontend/dist
    |-- /api/    -> 反向代理 127.0.0.1:8080
    `-- /uploads -> /www/wwwroot/uploads 或反向代理后端
    v
Spring Boot 后端（systemd: plant-cultivation）
    `-- /www/wwwroot/plant-cultivation/backend/app.jar
        v
MySQL 8.0（数据库 plant_cultivation）
```

---

## 三、服务器信息

| 项目 | 值 |
|------|-----|
| 服务器 | 腾讯云 `175.178.200.181`（CentOS，宝塔面板） |
| SSH 登录 | `ssh root@175.178.200.181` |
| JDK | `/www/server/java/jdk-17.0.8/bin/java` |
| 项目根目录 | `/www/wwwroot/plant-cultivation/` |
| 后端 jar | `/www/wwwroot/plant-cultivation/backend/app.jar` |
| 前端目录 | `/www/wwwroot/plant-cultivation/frontend/dist/` |
| 环境变量 | `/www/wwwroot/plant-cultivation/.env` |
| 上传图片 | `/www/wwwroot/uploads/` |
| 部署日志 | `/www/wwwroot/plant-cultivation/deploy.log` |
| systemd 服务 | `plant-cultivation` |

宝塔网页站点启停入口：宝塔面板 -> 网站 -> 对应站点 -> 设置/管理；后端启停看 systemd 命令。

---

## 四、常用运维命令

| 场景 | 命令 |
|------|------|
| 查看后端状态 | `systemctl status plant-cultivation` |
| 启动后端 | `systemctl start plant-cultivation` |
| 停止后端 | `systemctl stop plant-cultivation` |
| 重启后端 | `systemctl restart plant-cultivation` |
| 实时看后端日志 | `journalctl -u plant-cultivation -f` |
| 最近 30 条日志 | `journalctl -u plant-cultivation -n 30 --no-pager` |
| MySQL 状态 | `/etc/init.d/mysqld status` 或 `systemctl status mysqld` |
| 启动 MySQL | `/etc/init.d/mysqld start` 或 `systemctl start mysqld` |
| Nginx 状态 | `/etc/init.d/nginx status` |
| 重载 Nginx | `/etc/init.d/nginx reload` |
| 部署日志 | `tail -f /www/wwwroot/plant-cultivation/deploy.log` |
| 查看端口 | `ss -lntp \| grep -E ':80\|:443\|:8080'` |

---

## 五、部署目录约定

```text
/www/wwwroot/plant-cultivation/
├── backend/app.jar.new          # CI 或手动上传的新 jar
├── backend/app.jar              # 当前运行 jar
├── backend/app.jar.bak          # deploy.sh 自动备份
├── frontend/dist/               # 当前前端静态文件
├── frontend/dist.bak            # deploy.sh 自动备份
├── frontend/frontend-new/       # 新前端目录（兼容）
├── frontend-new/                # 新前端目录（兼容）
├── scripts/fill_missing_care_guides.sql
├── deploy.sh
└── .env
```

`frontend-new` 是部署过程中的临时目录。部署成功后脚本会把它移动为 `frontend/dist`。如果宝塔文件列表里长期看到 `frontend-new`，通常说明部署未执行、执行中断，或上传后还没有切换。

---

## 六、环境变量

文件位置：`/www/wwwroot/plant-cultivation/.env`，不要提交到 git。

```bash
DB_PASSWORD=你的数据库密码
JWT_SECRET=你的JWT密钥
UPLOAD_DIR=/www/wwwroot/uploads

# 可选
RUN_DB_PATCHES=1
DB_NAME=plant_cultivation
DB_USERNAME=plant_cultivation
DB_HOST=localhost
```

`DB_PASSWORD` 与 `JWT_SECRET` 是必需项。缺失会导致后端启动失败，这是有意设计，用于避免默认密钥带来的安全风险。

修改后重启：

```bash
systemctl restart plant-cultivation
```

---

## 七、数据库

- 数据库：`plant_cultivation`
- 编码：`utf8mb4`
- 初始化脚本：
  - `backend/src/main/resources/db/schema.sql`
  - `backend/src/main/resources/db/data.sql`
- 指南补齐脚本：`scripts/fill_missing_care_guides.sql`

补齐指南脚本会给缺少指南的植物补充：

- 浇水指南
- 光照指南
- 施肥指南
- 修剪指南
- 通用养护

脚本是幂等的，可重复执行，不会重复插入已有类型的指南。

手动执行：

```bash
mysql --default-character-set=utf8mb4 -u plant_cultivation -p plant_cultivation \
  < /www/wwwroot/plant-cultivation/scripts/fill_missing_care_guides.sql
```

如果不希望部署时自动执行数据库补丁，在 `.env` 设置：

```bash
RUN_DB_PATCHES=0
```

---

## 八、HTTPS / SSL

宝塔申请 SSL：

1. 宝塔面板 -> 网站 -> 选择站点 -> SSL
2. 选择 Let's Encrypt 或宝塔 SSL
3. 填写域名并申请证书
4. 确认 HTTPS 可访问后，再开启“强制 HTTPS”

腾讯云安全组放行 443：

1. 腾讯云控制台 -> 云服务器 CVM -> 安全组
2. 入站规则添加：
   - 协议端口：`TCP:443`
   - 来源：`0.0.0.0/0`
   - 策略：允许
3. 保留 `TCP:80`，用于 HTTP 访问和证书续期

检查命令：

```bash
ss -lntp | grep -E ':80|:443|:8080'
curl -I http://你的域名
curl -I https://你的域名
```

---

## 九、故障排查

### 部署失败

1. 看 GitHub Actions 的 Deploy 步骤日志。
2. 服务器上查看：

```bash
systemctl status plant-cultivation
tail -50 /www/wwwroot/plant-cultivation/deploy.log
journalctl -u plant-cultivation -n 50 --no-pager
```

### 网页打不开

1. 服务器检查 Nginx：`/etc/init.d/nginx status`
2. 检查端口：`ss -lntp | grep -E ':80|:443|:8080'`
3. 检查后端：`systemctl status plant-cultivation`
4. 如果手机能开、电脑打不开：重点检查电脑代理/VPN/浏览器缓存/DNS。
5. 如果提示 `ERR_CONNECTION_REFUSED`：重点检查 Nginx 是否监听 80/443，腾讯云安全组是否放行。

### 回滚

`deploy.sh` 切换前会备份旧版本。

```bash
mv /www/wwwroot/plant-cultivation/backend/app.jar.bak /www/wwwroot/plant-cultivation/backend/app.jar
rm -rf /www/wwwroot/plant-cultivation/frontend/dist
mv /www/wwwroot/plant-cultivation/frontend/dist.bak /www/wwwroot/plant-cultivation/frontend/dist
systemctl restart plant-cultivation
```

---

## 十、本地验证

每次提交前建议执行：

```bash
cd frontend
npm run build

cd ../backend
./gradlew test
./gradlew bootJar
```

最近一次验证：

- 前端 `npm run build`：通过
- 后端 `./gradlew test`：通过
- 后端 `./gradlew bootJar`：通过

> 当前 Sass 会提示 `@import "tailwindcss"` 弃用警告，不影响构建和线上运行。

---

维护时间：2026-08-05。已同步前端优化、上传压缩、统一错误、指南补齐 SQL、SSL/443 和宝塔部署说明。
