<div align="center">

# 🌱 Plant-Cultivation

**[中文](README.md) | [English](README.en.md)**

植物培育养护平台 — 集植物百科、养护指南、社区交流、种植日记与养护工具于一体的全栈应用。

</div>

## ✨ 核心功能

- **植物百科** — 51 种植物、约 100 个养护字段，涵盖光照、浇水、温湿度、施肥、修剪、土壤、毒性安全、四季养护、病虫害等，支持多条件筛选与分页
- **植物详情页** — 轻量动画、浅绿色卡片、滑动标签栏、指南缺失兜底展示
- **养护工具** — 浇水周期计算器（按盆径/环境/季节修正）、光照需求测试
- **社区交流** — 发帖、点赞、评论、图片上传、分类与植物标签筛选、游标分页、删除自己的帖子
- **植物日记** — 记录株高、叶片数、生长阶段、天气、心情与图片，支持时间线/卡片视图
- **用户中心** — 我的植物收藏、浇水提醒、资料与头像管理、修改密码
- **账号安全** — JWT + 令牌版本（服务端登出立即失效）、登录失败限流、上传内容多重校验
- **国际化** — 中文/英文双语，植物名称、分类、标签与主要界面文案随语言切换

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3.5 + TypeScript + Vite 6 + Pinia + Vue Router + vue-i18n + Tailwind CSS 4 + Sass + GSAP |
| 后端 | Spring Boot 3.4 + Spring Security + MyBatis-Plus 3.5 + JJWT 0.12 |
| 数据库 | MySQL 8.0 |
| 部署 | 宝塔 Nginx + systemd + GitHub Actions |

## 📁 项目结构

```
Plant-Cultivation/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/plantcultivation/
│   │   ├── config/                 # Security / CORS / WebMvc / MyBatis-Plus / JWT 过滤器
│   │   ├── controller/             # REST API 控制器
│   │   ├── service/                # 业务逻辑
│   │   ├── mapper/                 # MyBatis-Plus Mapper（注解 SQL）
│   │   ├── entity/                 # 数据库实体
│   │   ├── dto/                    # 请求体 + Bean Validation
│   │   ├── vo/                     # ResultVO / PageResultVO
│   │   ├── security/               # AuthPrincipal（请求上下文登录身份）
│   │   ├── util/                   # JWT、限流、图片校验、SecurityUtil
│   │   └── exception/              # 业务异常与全局异常处理
│   ├── src/main/resources/
│   │   ├── application.yml         # 配置
│   │   └── db/                     # schema.sql + data.sql
│   └── src/test/java/              # 单元测试
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── api/                    # axios 实例与各模块 API
│   │   ├── components/             # layout / sections / common
│   │   ├── composables/            # 滚动动画、后端重启自动刷新
│   │   ├── locales/                # 国际化文件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── types/                  # TypeScript 类型
│   │   └── views/                  # 页面组件
│   └── public/images/plants/       # 植物图片
├── scripts/                        # 部署与数据库工具脚本
│   ├── deploy.sh                   # 原子部署 / 重启 / 健康检查 / 回滚
│   ├── smoke-test.sh               # 部署后冒烟测试
│   ├── rebuild-sql.py              # 从生产 dump 重建 schema.sql / data.sql
│   ├── migrations/                 # 增量迁移与回滚脚本
│   └── fill_missing_care_guides.sql
├── .github/workflows/deploy.yml    # 构建与部署流水线
├── API_TEST_DOCUMENT.md            # 接口测试说明
└── DEPLOYMENT.md                   # 线上部署与排查说明
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 20+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 建库建表 + 种子数据（6 个分类、51 种植物、养护指南）
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql

# 可选：补齐缺失的植物养护指南（幂等，可重复执行）
mysql -u plant_cultivation -p plant_cultivation < scripts/fill_missing_care_guides.sql
```

已有数据的环境升级请使用 `scripts/migrations/`，见[数据库迁移](#-数据库迁移)。

### 2. 配置环境变量

后端启动时通过 `spring.config.import` 读取 `backend/.env`（文件缺失不报错，变量缺失会启动失败）：

```properties
DB_PASSWORD=你的数据库密码
JWT_SECRET=至少32字符的随机字符串     # openssl rand -base64 48
UPLOAD_DIR=/www/wwwroot/uploads       # 本地可改成 ./uploads
```

也可以按 `.env.example` 直接 export 环境变量。

### 3. 启动后端

```bash
cd backend
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

后端运行在 `http://localhost:8080`，健康检查 `GET /api/health`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev              # http://localhost:5173
```

Vite 已把 `/api` 与 `/uploads` 代理到 `localhost:8080`。

### 5. 构建与测试

```bash
# 前端类型检查 + 生产构建
cd frontend && npm run build

# 后端测试 / 发布 jar
cd ../backend && ./gradlew test && ./gradlew bootJar
```

构建产物：`frontend/dist/`、`backend/build/libs/*.jar`。

## 📊 数据库设计

| 表名 | 说明 |
|------|------|
| `plant` | 植物主表（约 100 列，JSON 字段存四季养护/病虫害/常见问题） |
| `category` / `plant_category` | 分类与多对多关联 |
| `care_guide` | 分季节养护指南 |
| `user` | 账号、资料、`token_version`（令牌版本） |
| `post` / `post_comment` / `post_like` | 社区（`user_id` 外键，点赞唯一键 `(post_id, user_id)`） |
| `plant_diary` | 植物生长日记（`user_id` 外键） |
| `user_plant_collection` | 用户收藏、浇水间隔与下次浇水时间 |
| `login_attempt` | 登录失败限流记录（持久化，多实例共享） |

**约定**：所有用户产生数据统一使用 `user_id`（bigint，外键 `user.id`，`ON DELETE CASCADE`）；`account` 只作为登录名保存在 `user` 表，不再作为其他表外键。

## 🔐 认证与安全

- **登录**：`POST /api/auth/login` 校验 BCrypt 密码，签发 24h JWT；claims 含 `sub`（账号）、`uid`（用户主键）、`ver`（令牌版本）。
- **服务端登出**：`POST /api/auth/logout` 令 `user.token_version + 1`。过滤器每次请求比对 `ver`，不一致即未登录 —— 旧 token 立即失效，且该账号所有设备同时退出。
- **修改密码**：同样轮换令牌版本，并把新令牌返回给当前设备，因此当前设备保持登录、其他设备自动退出。
- **登录限流**：账号与 IP 两个维度，10 分钟滑动窗口失败 5 次返回 429；记录落 MySQL，多实例/重启后依然生效，定时清理 24 小时前数据。
- **上传安全**：Content-Type + 魔数 + ImageIO 解码 + 单边 8000px 限制，超出 1920×1080 或 500KB 重编码压缩。
- **越权防护**：帖子/日记的更新删除校验 `user_id` 归属；未登录访问 `/api/**` 统一 401 JSON。

> 升级提示：改造前签发的旧 JWT 不含 `uid/ver`，升级后按未登录处理，用户重新登录一次即可。

## 🔧 配置说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_PASSWORD` | MySQL 用户 `plant_cultivation` 的密码 | 必填 |
| `JWT_SECRET` | JWT 密钥，生产必须高强度随机字符串 | 必填 |
| `UPLOAD_DIR` | 图片上传目录 | `/www/wwwroot/uploads` |
| `RUN_DB_PATCHES` | 部署时是否自动执行补丁 SQL | `1` |

线上 `.env` 放在 `/www/wwwroot/plant-cultivation/.env`；本地放在 `backend/.env`。

## 🌐 API 概览

公开接口（无需登录）：`GET /api/health`、`/api/plants/**`、`/api/categories/**`、`/api/posts/**`（GET）。

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` / `/api/auth/login` | 注册 / 登录 | 否 |
| GET | `/api/auth/me` | 当前用户 | 是 |
| PUT | `/api/auth/profile` | 修改资料 | 是 |
| PUT | `/api/auth/password` | 修改密码（返回新 token） | 是 |
| POST | `/api/auth/logout` | 登出（令牌立即失效） | 是 |
| GET | `/api/plants` | 植物列表（筛选 + 分页） | 否 |
| GET | `/api/plants/{slug}` | 植物详情 | 否 |
| GET | `/api/plants/{id}/care-guides` | 养护指南 | 否 |
| GET/POST/DELETE | `/api/posts/**` | 帖子、点赞、评论 | GET 否，其余是 |
| GET/POST/PUT/DELETE | `/api/diaries/**` | 植物日记 | 是 |
| GET/POST/PUT/DELETE | `/api/collections/**` | 收藏与浇水 | 是 |
| POST | `/api/upload/image` | 上传图片 | 是 |

统一响应 `{ "code": 200, "message": "success", "data": ... }`，错误时 `code` 与 HTTP 状态码一致。完整用例见 [API_TEST_DOCUMENT.md](API_TEST_DOCUMENT.md)。

## 🔁 数据库迁移

全新安装直接用 `schema.sql`；已有数据的环境：

```bash
# 1) 备份
mysqldump -u plant_cultivation -p plant_cultivation > backup-$(date +%F).sql

# 2) 迁移（脚本头部有孤儿数据自检，需先清理）
mysql -u plant_cultivation -p plant_cultivation < scripts/migrations/001-unify-user-identity.sql

# 3) 回滚
mysql -u plant_cultivation -p plant_cultivation < scripts/migrations/001-rollback.sql
```

迁移内容：`user_account` → `user_id`、新增 `user.token_version`、新增 `login_attempt`。
MySQL DDL 隐式提交，无法整体回滚，务必先备份。
`scripts/rebuild-sql.py` 从生产 dump（`plant_cultivation.sql`）重建 `schema.sql`/`data.sql` 时不会包含应用层变更，重建后需重新执行迁移。

## 🚢 宝塔部署

线上部署约定详见 [DEPLOYMENT.md](DEPLOYMENT.md)。

```text
/www/wwwroot/plant-cultivation/
├── backend/app.jar
├── backend/app.jar.new
├── frontend/dist/
├── frontend-new/ 或 frontend/frontend-new/
├── scripts/fill_missing_care_guides.sql
├── deploy.sh
└── .env
```

流水线：push `master` → 后端 `gradlew test bootJar`、前端 `npm ci && npm run build` → rsync 上传 → `deploy.sh` 备份、原子切换、重启 systemd、30 秒健康检查、失败回滚、nginx reload → `smoke-test.sh` 校验。

```bash
systemctl status plant-cultivation
systemctl restart plant-cultivation
journalctl -u plant-cultivation -f
tail -f /www/wwwroot/plant-cultivation/deploy.log
```

需要配置的 GitHub Secrets：`SERVER_HOST`、`SERVER_USER`、`SERVER_PORT`、`SERVER_SSH_KEY`。

## 🧪 测试

```bash
cd backend && ./gradlew test
```

覆盖 JWT claims 与旧令牌兼容、登录限流窗口/清零/清理、日记按 `user_id` 过滤与作者信息组装、收藏幂等与浇水时间推算、图片魔数校验。

## ❓ 常见问题

**启动报 `Could not resolve placeholder 'DB_PASSWORD'`**
`backend/.env` 缺变量；生产由 systemd `EnvironmentFile` 或部署脚本注入。

**图片 404**
`UPLOAD_DIR` 目录不存在，或 Nginx 未把 `/uploads` 指过去。

**中文搜索召回差**
`plant.ft_search` 使用 MySQL FULLTEXT，中文效果依赖服务端 ngram 分词配置。

**升级后所有用户被登出**
预期行为：旧令牌缺少 `uid/ver`，重新登录即可。

## 📝 License

MIT License
