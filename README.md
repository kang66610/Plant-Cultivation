<div align="center">

# 🌱 Plant-Cultivation

**[中文](#-核心功能) | [English](README.en.md)**

植物培育养护平台 — 一个集植物百科、养护指南、社区交流、种植日记与养护工具于一体的全栈应用。

</div>

## ✨ 核心功能

- **植物百科** — 收录 50+ 种植物的详细养护数据，涵盖光照、浇水、施肥、土壤、病虫害、毒性安全等 70+ 字段
- **植物详情页** — 轻量动画、浅绿色卡片、返回按钮、滑动标签栏、指南缺失兜底展示
- **养护指南** — 按植物生成浇水、光照、施肥、修剪、通用养护建议，支持补齐缺失指南 SQL
- **社区交流** — 发帖、点赞、评论、图片上传、植物标签搜索，适配移动端抽屉导航
- **植物日记** — 记录植物生长历程，支持图片、株高、叶片数、生长趋势图、时间线/卡片视图
- **用户中心** — 个人收藏、养护提醒、资料管理、头像上传
- **图片上传优化** — 前端上传前校验/压缩，后端魔数校验、尺寸限制、统一错误返回
- **国际化** — 中文/英文双语支持，植物名称、分类、标签与主要界面文案随语言切换

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Pinia + vue-i18n + Sass |
| 后端 | Spring Boot 3.4 + MyBatis Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 部署 | 宝塔 Nginx + systemd + GitHub Actions |

## 📁 项目结构

```
Plant-Cultivation/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/plantcultivation/
│   │       ├── config/         # 安全、CORS、JWT 配置
│   │       ├── controller/     # REST API 控制器
│   │       ├── entity/         # 数据库实体
│   │       ├── mapper/         # MyBatis Plus Mapper
│   │       ├── service/        # 业务逻辑层
│   │       ├── util/           # 工具类 (JWT)
│   │       └── vo/             # 视图对象
│   └── src/main/resources/
│       ├── application.yml     # 配置文件
│       └── db/                 # 数据库脚本
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 请求封装
│   │   ├── components/         # 组件
│   │   ├── composables/        # 组合式函数
│   │   ├── locales/            # 国际化文件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── types/              # TypeScript 类型
│   │   └── views/              # 页面组件
│   └── public/                 # 静态资源
├── scripts/                    # 部署与数据库工具脚本
│   ├── deploy.sh               # 宝塔服务器切换/重启/健康检查脚本
│   └── fill_missing_care_guides.sql
└── DEPLOYMENT.md               # 线上部署与排查说明
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 创建数据库并导入结构与种子数据
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql

# 可选：补齐缺失的植物养护指南（幂等，可重复执行）
mysql -u plant_cultivation -p plant_cultivation < scripts/fill_missing_care_guides.sql
```

### 2. 启动后端

```bash
cd backend

# 配置环境变量
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
export UPLOAD_DIR=/www/wwwroot/uploads

# 启动
./gradlew bootRun
```

后端运行在 `http://localhost:8080`

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端运行在 `http://localhost:5173`

### 4. 构建与测试

```bash
# 前端类型检查 + 生产构建
cd frontend
npm run build

# 后端测试
cd ../backend
./gradlew test

# 后端发布 jar
./gradlew bootJar
```

构建产物：

- 前端：`frontend/dist/`
- 后端：`backend/build/libs/*.jar`

## 📊 数据库设计

| 表名 | 说明 |
|------|------|
| `plant` | 植物主表（含养护参数、媒体、SEO） |
| `care_guide` | 分季节养护指南 |
| `category` | 植物分类 |
| `user` | 用户系统 |
| `post` | 社区帖子 |
| `post_comment` | 帖子评论 |
| `post_like` | 帖子点赞 |
| `plant_diary` | 植物生长日记 |
| `user_plant_collection` | 用户收藏与养护提醒 |

## 🔧 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_PASSWORD` | MySQL 用户 `plant_cultivation` 的密码 | 必填 |
| `JWT_SECRET` | JWT 密钥，生产环境必须使用高强度随机字符串 | 必填 |
| `UPLOAD_DIR` | 图片上传目录 | `/www/wwwroot/uploads` |
| `RUN_DB_PATCHES` | 部署时是否自动执行补丁 SQL | `1` |

线上 `.env` 建议放在：

```bash
/www/wwwroot/plant-cultivation/.env
```

### API 接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| GET | `/api/plants` | 获取植物列表 | 否 |
| GET | `/api/plants/{slug}` | 获取植物详情 | 否 |
| POST | `/api/upload/image` | 上传图片 | 是 |
| GET | `/api/posts` | 获取帖子列表 | 否 |
| POST | `/api/posts` | 发布帖子 | 是 |

## 🚢 宝塔部署

线上部署约定详见 [DEPLOYMENT.md](DEPLOYMENT.md)。

核心目录：

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

常用命令：

```bash
systemctl status plant-cultivation
systemctl restart plant-cultivation
journalctl -u plant-cultivation -f
tail -f /www/wwwroot/plant-cultivation/deploy.log
ss -lntp | grep -E ':80|:443|:8080'
```

`frontend-new` 是部署脚本用于临时接收新前端包的目录。部署成功后会被移动为 `frontend/dist`，正常情况下不会长期保留。

## ✅ 最近验证

- `npm run build`：通过
- `./gradlew test`：通过
- `./gradlew bootJar`：通过

> 当前 Sass 仍会提示 `@import "tailwindcss"` 弃用警告，不影响构建和线上运行。

## 📝 License

MIT License

