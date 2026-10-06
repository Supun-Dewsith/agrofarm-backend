# AgroFarm Management System — Backend API & AI Services

[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%2F4.x-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-blue.svg?logo=python)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-Microservice-009688.svg?logo=fastapi)](https://fastapi.tiangolo.com)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)](https://www.docker.com/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0202.svg?logo=flyway)](https://flywaydb.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20MySQL-336791.svg?logo=postgresql)](https://www.postgresql.org/)

The backend foundation for the **Agro-Trade Farming Management System**, delivering high-performance REST APIs, geospatial plot calculations, workforce biometric/QR attendance tracking, and AI-powered computer vision plant pathology via microservices.

---

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Key Features & Modules](#key-features--modules)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Environment Configuration](#environment-configuration)
- [Local Development Setup](#local-development-setup)
- [Fast Iteration Workflow](#fast-iteration-workflow)
- [Database Migrations (Flyway)](#database-migrations-flyway)
- [Core API Endpoints](#core-api-endpoints)
- [Production Deployment](#production-deployment)
- [Git & Commit Standards](#git--commit-standards)

---

## Architecture Overview

```
                      ┌──────────────────────────────────────┐
                      │    Client (Web / Mobile Apps)        │
                      └──────────────────┬───────────────────┘
                                         │  HTTP / REST (Port 80 / 443)
                                         ▼
                      ┌──────────────────────────────────────┐
                      │          Nginx Reverse Proxy         │
                      └──────────┬─────────────────┬─────────┘
                                 │                 │
                /api/*, /actuator│                 │ /ai/*
                                 ▼                 ▼
          ┌───────────────────────────┐    ┌───────────────────────────┐
          │  Spring Boot REST API     │◄──►│  Python AI Microservice   │
          │  (Java 21, Spring Sec)    │    │  (FastAPI + Gemini Vision)│
          └─────────────┬─────────────┘    └───────────────────────────┘
                        │
         ┌──────────────┴──────────────┐
         ▼                             ▼
┌──────────────────┐         ┌───────────────────┐
│ Relational DB    │         │ Supabase Storage  │
│ (PostgreSQL/SQL) │         │ (Images & Media)  │
└──────────────────┘         └───────────────────┘
```

The system is separated into three orchestrated container services:
1. **Nginx Reverse Proxy**: Routes inbound client traffic, handles compression, SSL termination, and rate limiting.
2. **Spring Boot Core API**: Handles business logic, role-based security, relational persistence, and transactional operations.
3. **Python AI Microservice**: Lightweight FastAPI service powered by Google Gemini Vision for plant disease diagnosis and pest pathology analysis.

---

## Key Features & Modules

- **Authentication & RBAC**: JWT token lifecycle with refresh token rotation, custom user permissions (`CAN_REGISTER_USERS`, `VIEW_FARMERS`, `VIEW_TASKS`, etc.), and automatic administrator seeding.
- **Worker Management**: Comprehensive worker profiles, assignment history, farm plot allocations, and contact records.
- **Farm & Field Geospatial Mapping**: Farm boundary definition with polygon GPS coordinates, field plot segmentation, and geocoding search.
- **Crop Lifecycle Tracking**: Variety recording, planting schedules, growth stage updates, and projected harvest windows.
- **Smart Attendance & Geofencing**: Real-time worker check-ins with time-sensitive QR code validation and GPS radius/proximity checks.
- **Operations & Task Scheduling**: Work task allocation by priority (Low, Medium, High, Urgent), assigned supervisors and field workers.
- **AI Plant Pathology & Disease Detection**: Image diagnosis via neural microservice, disease severity estimation, yield risk forecast, and Integrated Pest Management (IPM) treatment recommendations.
- **Harvest & Market Price Intelligence**: Harvest logging and comparison against official market prices to determine profitability and margins.
- **Inventory & Resource Management**: Seed, fertilizer, chemical, and equipment stock monitoring with low-stock alerts.
- **Weather Telemetry & Planning**: Real-time coordinates-based weather conditions, humidity, precipitation, and agricultural forecasts.
- **Financials & Analytics**: Revenue vs. expense statements, farm profitability indexes, and downloadable reports.

---

## Tech Stack

- **Language & Framework:** Java 21 (Eclipse Temurin JDK), Spring Boot 3.x / 4.x
- **Data & Persistence:** Spring Data JPA, Hibernate, PostgreSQL / MySQL
- **Schema Management:** Flyway Migrations (V1 – V23)
- **Security:** Spring Security, JJWT (`0.12.6`), BCrypt Password Encoding
- **Microservices & AI:** Python 3.11, FastAPI, Uvicorn, Google Gemini Vision API
- **Storage:** Supabase Storage (Object store for images and user avatars)
- **Reverse Proxy:** Nginx Alpine
- **Containerization:** Docker & Docker Compose

---

## Prerequisites

Ensure the following tools are installed on your host machine:
- [Docker Desktop](https://docs.docker.com/get-docker/) (with Docker Compose v2)
- [Java 21 JDK](https://adoptium.net/temurin/releases/?version=21)
- [Git](https://git-scm.com/)
- Maven (or use the included `./mvnw` / `mvnw.cmd`)

---

## Project Structure

```
agrofarm-backend/
├── .env.example                      # Environment variables template
├── docker-compose.yml                # Local development orchestration
├── docker-compose.prod.yml           # Production VM orchestration
├── nginx/
│   ├── app.local.conf                # Local development proxy config
│   └── app.prod.conf                 # Production SSL/proxy config
├── python-microservice/
│   ├── Dockerfile                    # Python FastAPI Dockerfile
│   ├── requirements.txt              # Microservice Python packages
│   └── ai_service.py                 # Plant disease detection logic
└── agrofarm-backend/
    ├── pom.xml                       # Maven build descriptor
    ├── mvnw / mvnw.cmd               # Maven wrapper scripts
    └── src/
        ├── main/
        │   ├── java/com/agrofarm/api/
        │   │   ├── config/           # Security, CORS, Swagger configs
        │   │   ├── controller/       # REST API Endpoints
        │   │   ├── dto/              # Request / Response DTOs
        │   │   ├── entity/           # JPA Database Entities
        │   │   ├── repository/       # Data Access Layer & Implementations
        │   │   ├── security/         # JWT filter & authentication provider
        │   │   └── service/          # Business logic & integrations
        │   └── resources/
        │       ├── application.yml   # App properties
        │       └── db/migration/     # Flyway SQL migrations (V1 - V23)
        └── test/                     # Unit & Integration tests
```

---

## Environment Configuration

1. Copy the sample environment file:
   ```bash
   cp .env.example .env
   ```
2. Configure the required environment variables in `.env`:

| Variable | Description | Example / Default |
| --- | --- | --- |
| `DB_URL` | JDBC Connection String | `jdbc:postgresql://postgres:5432/agrofarm` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `your_secure_password` |
| `JWT_SECRET` | 256-bit secret string for signing JWTs | `base64_encoded_secret_string` |
| `JWT_EXPIRATION` | Access token lifespan in milliseconds | `86400000` (24 Hours) |
| `ADMIN_USERNAME` | Default system administrator username | `admin` |
| `ADMIN_EMAIL` | Default system administrator email | `admin@agrofarm.com` |
| `ADMIN_PASSWORD` | Default system administrator password | `SecureAdminPass123!` |
| `GEMINI_API_KEY` | Google Gemini API key for AI pathology | `AIzaSy...` |
| `CORS_ALLOWED_ORIGINS` | Permitted web clients (comma-separated) | `http://localhost:5173,http://localhost` |
| `SUPABASE_URL` | Supabase project URL | `https://xxxx.supabase.co` |
| `SUPABASE_SERVICE_KEY`| Supabase service role API key | `eyJhbGci...` |
| `SUPABASE_BUCKET_NAME`| Supabase media storage bucket name | `avatars` |

---

## Local Development Setup

### 1. Build the Spring Boot JAR
The local Docker container uses a volume mount pointing to `/target` for rapid restarts. Build the JAR once before starting Docker:

```bash
# On Windows (Command Prompt / PowerShell):
cd agrofarm-backend
.\mvnw.cmd clean package -DskipTests
cd ..

# On Linux / macOS / WSL:
cd agrofarm-backend && ./mvnw clean package -DskipTests && cd ..
```

### 2. Start the Services
Launch all containers in detached mode:

```bash
docker compose up -d
```

### 3. Verify Health
- **Nginx & Backend Health:** `http://localhost/actuator/health`
- **Python AI Health:** `http://localhost/ai/health` (or `http://localhost:8000/health`)

---

## Fast Iteration Workflow

When modifying Java source code, you do not need to rebuild the Docker images. Simply compile the JAR and restart the backend container:

```bash
# 1. Repackage the backend (approx. 4 seconds)
cd agrofarm-backend && ./mvnw package -DskipTests && cd ..

# 2. Restart the container instantly
docker compose restart agrofarm-backend
```

### Useful Docker Commands
```bash
# Follow logs across all containers
docker compose logs -f

# Follow logs for the Spring Boot service only
docker compose logs -f agrofarm-backend

# Follow logs for the Python AI service only
docker compose logs -f python-microservice

# Stop all containers
docker compose down
```

---

## Database Migrations (Flyway)

Flyway handles automatic schema migration on application startup. Migration scripts reside in:
`agrofarm-backend/src/main/resources/db/migration/`

| Version | Migration Focus |
| --- | --- |
| `V1` | Initial `users` schema & credentials |
| `V2` | Role permissions & authorizations |
| `V3` – `V6` | Farms, weather data, attendance, and field plots |
| `V7` – `V10`| Task assignments, field boundaries, and workload tracking |
| `V12` – `V15`| Crop management, user settings, and GPS attendance |
| `V16` – `V19`| Dynamic QR code check-ins, notifications, finances & inventory |
| `V20` – `V23`| Disease detection records, official market prices, and biological profile fields |

> **Note:** Never modify an already executed migration script in production. Always create a new versioned migration (e.g. `V24__your_change.sql`).

---

## Core API Endpoints

### Authentication & Users
- `POST /api/auth/login` — Authenticate and receive JWT access & refresh tokens
- `POST /api/auth/refresh` — Refresh access token
- `POST /api/users/register` — Register staff / supervisors
- `GET /api/worker/workers` — List registered farm workers

### Farms & Fields
- `GET /api/farms` — Get all farms with geospatial boundaries
- `POST /api/farms` — Create new farm plot
- `GET /api/fields` — List fields allocated to farms
- `GET /api/farms/search` — Geocoding location lookup

### Attendance & Verification
- `POST /api/attendance/check-in` — Check-in via GPS coordinates
- `POST /api/attendance/qr-code` — Generate / validate dynamic attendance QR
- `GET /api/attendance/daily` — Daily attendance rosters

### Crops & Tasks
- `GET /api/crops` — List crops and growth progress
- `POST /api/crops` — Register crop planting
- `GET /api/tasks` — List tasks with status and priority
- `PATCH /api/tasks/{id}/status` — Update task lifecycle state

### AI Disease Detection
- `POST /api/disease/analyze` — Submit plant leaf image for neural diagnosis
- `GET /api/disease/history` — Fetch historical disease inspections & plots
- `POST /api/disease/remediation-task` — Convert recommendation into a farm task

### Harvest, Market & Inventory
- `GET /api/harvest` — Retrieve harvest records
- `GET /api/official-market/prices` — Fetch current official market rate benchmarks
- `GET /api/inventory` — Monitor stock levels and consumption

---

## Production Deployment

Automated deployments run via GitHub Actions (`.github/workflows/deploy.yml`) on push to the release branch.

To deploy manually on an Azure or Linux VM:
```bash
# Pull latest changes
git pull origin main

# Build production containers and launch
docker compose -f docker-compose.prod.yml up -d --build
```

---

## Git & Commit Standards

We adhere to the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <short imperative description>
```

### Commit Types
| Type | Description |
| --- | --- |
| `feat` | New feature or capability |
| `fix` | Bug fix |
| `refactor` | Code refactoring without behavioral changes |
| `perf` | Performance improvement |
| `docs` | Documentation updates |
| `chore` | Build scripts, dependencies, or tool configurations |
| `ci` | CI/CD pipeline modifications |

---

## License
This project is proprietary and confidential. Developed for the Agro-Trade Farming Management System.
