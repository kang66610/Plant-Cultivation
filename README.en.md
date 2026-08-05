<div align="center">

# 🌱 Plant-Cultivation

**[中文](README.md) | [English](#-key-features)**

A plant cultivation and care platform — a full-stack application integrating plant encyclopedia, care guides, community features, plant diary, and care tools.

</div>

## ✨ Key Features

- **Plant Encyclopedia** — 50+ plants with 70+ care fields covering light, watering, fertilizer, soil, pests, toxicity, and more
- **Plant Detail Pages** — Soft green cards, subtle animation, back button, sliding tab indicator, and fallback care-guide rendering
- **Care Guides** — Watering, light, fertilizer, pruning, and general care content, with an idempotent SQL patch for missing guides
- **Community** — Posts, likes, comments, image uploads, searchable plant tags, and mobile drawer navigation
- **Plant Diary** — Growth records with photos, height, leaf count, growth trend chart, timeline view, and card view
- **User Center** — Collections, watering reminders, profile management, and avatar upload
- **Image Upload Optimization** — Client-side validation/compression plus server-side image validation and unified error responses
- **Internationalization** — Chinese/English support for UI, plant names, categories, and key labels

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Vue 3 + TypeScript + Vite + Pinia + vue-i18n + Sass |
| Backend | Spring Boot 3.4 + MyBatis Plus + Spring Security + JWT |
| Database | MySQL 8.0 |
| Deployment | BT Panel Nginx + systemd + GitHub Actions |

## 📁 Project Structure

```
Plant-Cultivation/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/plantcultivation/
│   │       ├── config/         # Security, CORS, JWT Config
│   │       ├── controller/     # REST API Controllers
│   │       ├── entity/         # Database Entities
│   │       ├── mapper/         # MyBatis Plus Mappers
│   │       ├── service/        # Business Logic
│   │       ├── util/           # Utilities (JWT)
│   │       └── vo/             # View Objects
│   └── src/main/resources/
│       ├── application.yml     # Configuration
│       └── db/                 # Database Scripts
├── frontend/                   # Vue 3 Frontend
│   ├── src/
│   │   ├── api/                # API clients
│   │   ├── components/         # Components
│   │   ├── composables/        # Composables
│   │   ├── locales/            # i18n Files
│   │   ├── router/             # Router Config
│   │   ├── stores/             # Pinia State Management
│   │   ├── types/              # TypeScript Types
│   │   └── views/              # Page Components
│   └── public/                 # Static Assets
├── scripts/                    # Deployment and database utility scripts
│   ├── deploy.sh               # Server switch/restart/health-check script
│   └── fill_missing_care_guides.sql
└── DEPLOYMENT.md               # Production deployment and troubleshooting guide
```

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. Database Setup

```bash
# Create database and import schema/data
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql

# Optional: fill missing plant care guides (idempotent)
mysql -u plant_cultivation -p plant_cultivation < scripts/fill_missing_care_guides.sql
```

### 2. Start Backend

```bash
cd backend

# Configure environment variables
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
export UPLOAD_DIR=/www/wwwroot/uploads

# Start
./gradlew bootRun
```

Backend runs at `http://localhost:8080`

### 3. Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

Frontend runs at `http://localhost:5173`

### 4. Build & Test

```bash
# Frontend type check + production build
cd frontend
npm run build

# Backend tests
cd ../backend
./gradlew test

# Backend release jar
./gradlew bootJar
```

Build artifacts:

- Frontend: `frontend/dist/`
- Backend: `backend/build/libs/*.jar`

## 📊 Database Design

| Table | Description |
|-------|-------------|
| `plant` | Main plant table (care params, media, SEO) |
| `care_guide` | Seasonal care guides |
| `category` | Plant categories |
| `user` | User system |
| `post` | Community posts |
| `post_comment` | Post comments |
| `post_like` | Post likes |
| `plant_diary` | Plant growth diary |
| `user_plant_collection` | User collection & care reminders |

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | Password for MySQL user `plant_cultivation` | Required |
| `JWT_SECRET` | JWT secret. Use a strong random value in production | Required |
| `UPLOAD_DIR` | Image upload directory | `/www/wwwroot/uploads` |
| `RUN_DB_PATCHES` | Whether deployment applies patch SQL automatically | `1` |

Recommended production `.env` path:

```bash
/www/wwwroot/plant-cultivation/.env
```

### API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/register` | User registration | No |
| POST | `/api/auth/login` | User login | No |
| GET | `/api/plants` | List plants | No |
| GET | `/api/plants/{slug}` | Plant details | No |
| POST | `/api/upload/image` | Upload image | Yes |
| GET | `/api/posts` | List posts | No |
| POST | `/api/posts` | Create post | Yes |

## 🚢 BT Panel Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for the full production guide.

Main production paths:

```text
/www/wwwroot/plant-cultivation/
├── backend/app.jar
├── backend/app.jar.new
├── frontend/dist/
├── frontend-new/ or frontend/frontend-new/
├── scripts/fill_missing_care_guides.sql
├── deploy.sh
└── .env
```

Common commands:

```bash
systemctl status plant-cultivation
systemctl restart plant-cultivation
journalctl -u plant-cultivation -f
tail -f /www/wwwroot/plant-cultivation/deploy.log
ss -lntp | grep -E ':80|:443|:8080'
```

`frontend-new` is the temporary directory used by the deployment script for a new frontend build. After a successful deployment it is moved to `frontend/dist`.

## ✅ Last Verified

- `npm run build`: passed
- `./gradlew test`: passed
- `./gradlew bootJar`: passed

> Sass currently prints a deprecation warning for `@import "tailwindcss"`. It does not block production builds.

## 📝 License

MIT License
