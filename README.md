

# agrofarm-backend

## Prerequisites
- [Docker Desktop](https://docs.docker.com/get-docker/) (includes Docker Compose)
- [Java 21 JDK](https://adoptium.net/temurin/releases/?version=21)
- Git

---

## Getting Started

### 1. Update your local branch
```bash
git checkout my-branch
git fetch origin dev
git rebase origin/dev

```

### 2. Environment Setup

Copy the sample environment file and adjust any credentials if needed:

```bash
cp .env.example .env

```

---

## Local Development (HTTP)

Runs the entire stack locally over HTTP on port `80` using `docker-compose.yml` and `nginx/app.local.conf`.

### 1. Initial Build & First Run

Because the backend runs directly from the locally packaged JAR via a volume mount for faster development, package the Spring Boot application once before starting Docker:

```bash
# Package the Spring Boot JAR (Windows CMD / PowerShell)
cd agrofarm-backend
.\mvnw.cmd clean package -DskipTests
cd ..

# Alternatively on macOS / Linux / WSL:
cd agrofarm-backend && ./mvnw clean package -DskipTests && cd ..

# Start all services
docker compose up -d

```
### 2. Fast Development Workflow (Applying Code Changes)

When you make changes to Java code in `agrofarm-backend`, you do not need to rebuild Docker images. Simply repackage the JAR locally and restart the container:

```bash
# Step 1: Repackage the backend (3–5 seconds)
# Windows CMD / PowerShell:
cd agrofarm-backend
.\mvnw.cmd package -DskipTests
cd ..

# Alternatively on macOS / Linux / WSL:
cd agrofarm-backend && ./mvnw package -DskipTests && cd ..

# Step 2: Restart the container to apply changes (~1 second)
docker compose restart agrofarm-backend
```
### 3. Common Docker Commands

```bash
# Stream all container logs
docker compose logs -f

# Stream logs for the backend only
docker compose logs -f agrofarm-backend

# Stop all services
docker compose down
```

* **Backend API & Health Check:** `http://localhost/actuator/health`
* **Python AI Microservice:** `http://localhost/ai/`

---

## Production Deployment

Production runs automatically on the remote Azure VM via GitHub Actions (`.github/workflows/deploy.yml`).

To run manually on the server:

```bash
docker compose -f docker-compose.prod.yml up -d --build

```

---

## Git & Commit Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification for our commit messages.

### Commit Message Structure

Each commit message consists of a **type**, an optional **scope**, and a **description**:

`<type>(<scope>): <short summary>`

#### Examples

* `feat(dashboard): add activity feed component`
* `fix(auth): resolve JWT expiration bug on refresh`
* `docs(readme): update git commit guidelines`
* `refactor(user-service): decouple security principal from database entity`
* `chore(deps): update spring boot to v3.2.0`

---

### Allowed Types

| Type | Description | Example |
| --- | --- | --- |
| `feat` | A new feature for the user | `feat(charts): add real-time crop analysis chart` |
| `fix` | A bug fix | `fix(api): fix null pointer on missing headers` |
| `docs` | Documentation changes only | `docs(api): add OpenAPI endpoint docs` |
| `style` | Formatting, missing semi-colons, no code change | `style(backend): format imports and clean spacing` |
| `refactor` | Code restructuring without fixing a bug or adding a feature | `refactor(auth): simplify custom security filter logic` |
| `perf` | Code changes that improve performance | `perf(db): add index to user email lookup` |
| `test` | Adding missing tests or correcting existing tests | `test(auth): add unit tests for token refresh` |
| `chore` | Build process, auxiliary tools, or dependency updates | `chore(docker): install wget for healthcheck` |
| `ci` | Changes to CI configuration files and scripts | `ci(github): update deploy.yml to use prod compose file` |

---

### Rules to Follow

1. **Imperative Mood:** Use imperative, present tense in the description (*"add"* not *"added"* or *"adds"*).
2. **Lowercase Scope & Type:** Keep types and scopes in lowercase.
3. **No Period:** Do not end the subject line with a period.
4. **Atomic Commits:** Keep commits small and focused on a single change or fix.
