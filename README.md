

# agrofarm-backend

## Prerequisites
- [Docker Desktop](https://docs.docker.com/get-docker/) (includes Docker Compose)
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

Runs the entire stack locally over HTTP on port `80` using `docker-compose.yml` and `nginx/app.local.conf`:

```bash
# Start all services with fresh builds
docker compose up -d --build

# Stream logs
docker compose logs -f

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
