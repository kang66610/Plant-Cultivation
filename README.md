# 🌱 Plant-Cultivation

植物培育养护平台 — 一个集植物百科、养护指南、社区交流于一体的全栈应用。

## ✨ 核心功能

- **植物百科** — 收录 50+ 种植物的详细养护数据，涵盖光照、浇水、施肥、土壤、病虫害等 70+ 字段
- **养护指南** — 按季节分类的个性化养护建议，支持浇水计算器
- **社区交流** — 发帖、点赞、评论，分享养花心得
- **植物日记** — 记录植物生长历程，支持图片、株高、叶片数等数据追踪
- **用户中心** — 个人收藏、养护提醒、资料管理
- **3D 展示** — 支持 Three.js 植物模型渲染
- **国际化** — 中文/英文双语支持

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Three.js + Tailwind CSS |
| 后端 | Spring Boot 3.4 + MyBatis Plus + Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 国际化 | vue-i18n |

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
└── scripts/                    # 工具脚本
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 创建数据库并导入数据
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql
```

### 2. 启动后端

```bash
cd backend

# 配置环境变量（或修改 application.yml）
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret

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
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | - |
| `JWT_SECRET` | JWT 密钥 | - |

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


