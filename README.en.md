# 🌱 Plant-Cultivation

A plant cultivation and care platform — a full-stack application integrating plant encyclopedia, care guides, and community features.

## ✨ Key Features

- **Plant Encyclopedia** — 50+ plants with detailed care data covering 70+ fields: light, watering, fertilizer, soil, pests, etc.
- **Care Guides** — Seasonal personalized care tips with watering calculator
- **Community** — Post, like, and comment to share gardening experiences
- **Plant Diary** — Track plant growth with photos, height, leaf count, and more
- **User Center** — Personal collection, care reminders, profile management
- **3D Display** — Three.js plant model rendering support
- **Internationalization** — Chinese/English bilingual support

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Vue 3 + TypeScript + Vite + Element Plus + Three.js + Tailwind CSS |
| Backend | Spring Boot 3.4 + MyBatis Plus + Spring Security + JWT |
| Database | MySQL 8.0 |
| i18n | vue-i18n |

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
│   │   ├── api/                # API Request封装
│   │   ├── components/         # Components
│   │   ├── composables/        # Composables
│   │   ├── locales/            # i18n Files
│   │   ├── router/             # Router Config
│   │   ├── stores/             # Pinia State Management
│   │   ├── types/              # TypeScript Types
│   │   └── views/              # Page Components
│   └── public/                 # Static Assets
└── scripts/                    # Utility Scripts
```

## 🚀 Quick Start

### Prerequisites

- JDK 17+
- Node.js 18+
- MySQL 8.0+

### 1. Database Setup

```bash
# Create database and import data
mysql -u root -p < backend/src/main/resources/db/schema.sql
mysql -u root -p plant_cultivation < backend/src/main/resources/db/data.sql
```

### 2. Start Backend

```bash
cd backend

# Configure environment variables (or edit application.yml)
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret

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
| `DB_USERNAME` | Database username | root |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT secret key | - |

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

## 📝 License

MIT License
