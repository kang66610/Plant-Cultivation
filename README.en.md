<div align="center">

# 🌱 Plant-Cultivation

**[中文](README.md) | [English](README.en.md)**

A plant cultivation platform — a full-stack application combining a plant encyclopedia, care guides, community, growth diary, and care tools.

</div>

## ✨ Key Features

- **Plant Encyclopedia** — 51 plants with ~100 care fields (light, water, humidity, fertilizer, pruning, soil, toxicity, seasonal care, pests), with multi-condition filtering and pagination
- **Plant Detail Pages** — Light animation, soft green cards, sliding tab indicator, fallback rendering when a guide is missing
- **Care Tools** — Watering schedule calculator (pot size / environment / season adjustments) and a light-requirement quiz
- **Community** — Posts with images, likes, comments, category/plant-tag filters, cursor pagination, delete your own posts
- **Plant Diary** — Track height, leaf count, growth stage, weather, mood, and photos; timeline and card views
- **User Center** — Plant collections, watering reminders, profile/avatar management, password change
- **Account Security** — JWT with server-side token versioning (logout invalidates immediately), login rate limiting, strict upload validation
- **Internationalization** — Chinese/English for UI, plant names, categories, and key labels

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Vue 3.5 + TypeScript + Vite 6 + Pinia + Vue Router + vue-i18n + Tailwind CSS 4 + Sass + GSAP |
| Backend | Spring Boot 3.4 + Spring Security + MyBatis-Plus 3.5 + JJWT 0.12 |
| Database | MySQL 8.0 |
| Deployment | BT Panel Nginx + systemd + GitHub Actions |

## 📁 Project Structure

```
Plant-Cultivation/
├── backend/                        # Spring Boot backend
│   ├── src/main/java/com/plantcultivation/
│   │   ├── config/                 # Security / CORS / WebMvc / MyBatis-Plus / JWT filter
│   │   ├── controller/             # REST controllers
│   │   ├── service/                # Business logic
│   │   ├── mapper/                 # MyBatis-Plus mappers (annotation SQL)
│   │   ├── entity/                 # Database entities
│   │   ├── dto/                    # Request bodies + Bean Validation
│   │   ├── vo/                     # ResultVO / PageResultVO
│   │   ├── security/               # AuthPrincipal (authenticated identity)
│   │   ├── util/                   # JWT, rate limiter, image validator, SecurityUtil
│   │   └── exception/              # Business exception + global handler
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/                     # schema.sql + data.sql
│   └── src/test/java/              # Unit tests
├── frontend/                       # Vue 3 frontend
│   ├── src/
│   │   ├── api/                    # axios instance and module APIs
│   │   ├── components/             # layout / sections / common
│   │   ├── composables/            # Scroll animation, backend auto-reload
│   │   ├── locales/                # i18n files
│   │   ├── router/                 # Router config
│   │   ├── stores/                 # Pinia stores
│   │   ├── types/                  # TypeScript types
│   │   └── views/                  # Page components
│   └── public/images/plants/       # Plant images
├── scripts/                        # Deployment and database tooling
│   ├── deploy.sh                   # Atomic deploy / restart / health check / rollback
│   ├── smoke-test.sh               # Post-deploy smoke test
│   ├── rebuild-sql.py              # Rebuild schema.sql / data.sql from a production dump
│   ├── migrations/                 # Incremental migration and rollback scripts
│   └── fill_missing_care_guides.sql
├── .github/workflows/deploy.yml    # Build & deploy pipeline
├── API_TEST_DOCUMENT.md            # API test cases
└── DEPLOYMENT.md                   # Production deployment guide
```

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Node.js 20+
- MySQL 8.0+

### 1. Database Setup

```bash
# Schema + seed data (6 categories, 51 plants, care guides)
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql

# Optional: fill missing care guides (idempotent)
mysql -u plant_cultivation -p plant_cultivation < scripts/fill_missing_care_guides.sql
```

For an existing database, use `scripts/migrations/` instead — see [Database Migration](#-database-migration).

### 2. Configure Environment

The backend reads `backend/.env` through `spring.config.import` (missing file is fine; missing variables fail fast):

```properties
DB_PASSWORD=your_database_password
JWT_SECRET=a-random-string-of-at-least-32-chars   # openssl rand -base64 48
UPLOAD_DIR=/www/wwwroot/uploads                   # use ./uploads locally
```

You can also export the variables listed in `.env.example`.

### 3. Start Backend

```bash
cd backend
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

Runs at `http://localhost:8080`; health check: `GET /api/health`.

### 4. Start Frontend

```bash
cd frontend
npm install
npm run dev              # http://localhost:5173
```

Vite proxies `/api` and `/uploads` to `localhost:8080`.

### 5. Build & Test

```bash
cd frontend && npm run build
cd ../backend && ./gradlew test && ./gradlew bootJar
```

Artifacts: `frontend/dist/`, `backend/build/libs/*.jar`.

## 📊 Database Design

| Table | Description |
|-------|-------------|
| `plant` | Main plant table (~100 columns; JSON fields for seasonal care, pests, FAQs) |
| `category` / `plant_category` | Categories and many-to-many links |
| `care_guide` | Seasonal care guides |
| `user` | Account, profile, `token_version` |
| `post` / `post_comment` / `post_like` | Community (`user_id` FK, unique `(post_id, user_id)`) |
| `plant_diary` | Growth diary (`user_id` FK) |
| `user_plant_collection` | Collections, watering interval, next watering time |
| `login_attempt` | Failed login attempts (persisted, shared across instances) |

**Convention**: every user-owned table uses `user_id` (bigint FK to `user.id`, `ON DELETE CASCADE`). `account` remains only as the login name on `user` and is no longer a foreign key elsewhere.

## 🔐 Auth & Security

- **Login**: `POST /api/auth/login` verifies the BCrypt hash and issues a 24h JWT with `sub` (account), `uid` (user id), `ver` (token version).
- **Server-side logout**: `POST /api/auth/logout` increments `user.token_version`. The filter compares `ver` on every request, so old tokens stop working immediately — and all devices of that account are signed out.
- **Password change**: also rotates the token version, then returns a token signed with the new version to the current device, so it stays logged in while other devices are signed out.
- **Login rate limit**: per account and per IP, 5 failures in a 10-minute sliding window → 429. Attempts live in MySQL, so limits survive restarts and work across instances; a scheduled job purges records older than 24h.
- **Upload safety**: Content-Type + magic bytes + ImageIO decoding + 8000px per-side cap; images beyond 1920×1080 or 500KB are re-encoded.
- **Authorization**: post/diary update and delete verify `user_id` ownership; unauthenticated `/api/**` returns 401 JSON.

> Upgrade note: JWTs issued before this change lack `uid/ver` and are treated as unauthenticated. Users simply log in again.

## 🔧 Configuration

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | Password for MySQL user `plant_cultivation` | Required |
| `JWT_SECRET` | JWT secret; use a strong random value in production | Required |
| `UPLOAD_DIR` | Image upload directory | `/www/wwwroot/uploads` |
| `RUN_DB_PATCHES` | Whether deployment applies patch SQL automatically | `1` |

Production `.env`: `/www/wwwroot/plant-cultivation/.env`; local: `backend/.env`.

## 🌐 API Overview

Public (no auth): `GET /api/health`, `/api/plants/**`, `/api/categories/**`, `/api/posts/**` (GET only).

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/register` / `/api/auth/login` | Register / login | No |
| GET | `/api/auth/me` | Current user | Yes |
| PUT | `/api/auth/profile` | Update profile | Yes |
| PUT | `/api/auth/password` | Change password (returns new token) | Yes |
| POST | `/api/auth/logout` | Logout (invalidates tokens) | Yes |
| GET | `/api/plants` | List plants (filters + pagination) | No |
| GET | `/api/plants/{slug}` | Plant details | No |
| GET | `/api/plants/{id}/care-guides` | Care guides | No |
| GET/POST/DELETE | `/api/posts/**` | Posts, likes, comments | GET no, others yes |
| GET/POST/PUT/DELETE | `/api/diaries/**` | Plant diary | Yes |
| GET/POST/PUT/DELETE | `/api/collections/**` | Collections and watering | Yes |
| POST | `/api/upload/image` | Upload image | Yes |

Responses use `{ "code": 200, "message": "success", "data": ... }`; error `code` matches the HTTP status. Full cases in [API_TEST_DOCUMENT.md](API_TEST_DOCUMENT.md).

## 🔁 Database Migration

Fresh installs use `schema.sql`. Existing databases:

```bash
# 1) Backup
mysqldump -u plant_cultivation -p plant_cultivation > backup-$(date +%F).sql

# 2) Migrate (the script starts with orphan-data checks)
mysql -u plant_cultivation -p plant_cultivation < scripts/migrations/001-unify-user-identity.sql

# 3) Roll back if needed
mysql -u plant_cultivation -p plant_cultivation < scripts/migrations/001-rollback.sql
```

The migration rewrites `user_account` to `user_id`, adds `user.token_version`, and creates `login_attempt`.
MySQL DDL commits implicitly, so the script cannot roll back as a whole — always back up first.
`scripts/rebuild-sql.py` regenerates `schema.sql`/`data.sql` from the production dump (`plant_cultivation.sql`) and does not include application-level changes; re-apply the migrations afterwards.

## 🚢 BT Panel Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for the full production guide.

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

Pipeline: push to `master` → backend `gradlew test bootJar`, frontend `npm ci && npm run build` → rsync upload → `deploy.sh` backs up, swaps atomically, restarts systemd, waits up to 30s for health, rolls back on failure, reloads nginx → `smoke-test.sh` verifies endpoints.

```bash
systemctl status plant-cultivation
systemctl restart plant-cultivation
journalctl -u plant-cultivation -f
tail -f /www/wwwroot/plant-cultivation/deploy.log
```

Required GitHub Secrets: `SERVER_HOST`, `SERVER_USER`, `SERVER_PORT`, `SERVER_SSH_KEY`.

## 🧪 Tests

```bash
cd backend && ./gradlew test
```

Coverage: JWT claims and legacy-token handling, rate-limit window/reset/purge, diary filtering by `user_id` and author enrichment, collection idempotency and watering-date math, image magic-byte validation.

## ❓ FAQ

**`Could not resolve placeholder 'DB_PASSWORD'`**
`backend/.env` is missing variables; production injects them via systemd `EnvironmentFile` or the deploy script.

**Images 404**
`UPLOAD_DIR` does not exist, or Nginx does not map `/uploads` to it.

**Poor Chinese search recall**
`plant.ft_search` uses MySQL FULLTEXT; Chinese quality depends on the server's ngram parser configuration.

**All users are signed out after upgrading**
Expected: old tokens lack `uid/ver`. Log in again.

## 📝 License

MIT License
