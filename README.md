# farming-management-system-backend

## Getting Started
1. Copy `application.example.yml` to `application.yml` and update your local database credentials.
2. Run migrations (Flyway is located in `resources/db/migration`).

## Git Guidelines (Reliable Workflow)
To keep our git history clean and reliable, please follow these steps:

**Pull the latest changes before starting:**
   ```bash
    git checkout my-branch
    git fetch origin dev
    git rebase origin/dev
    git push origin my-branch --force-with-lease
 ```
    
    
   ##  Git & Commit Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification for our commit messages. This helps maintain a clean git history and automates versioning.

###  Commit Message Structure

Each commit message consists of a **type**, an optional **scope**, and a **description**:

`<type>(<scope>): <short summary>`

#### Examples
- `feat(dashboard): add activity feed component`
- `fix(auth): resolve JWT expiration bug on refresh`
- `docs(readme): update git commit guidelines`
- `refactor(user-service): decouple security principal from database entity`
- `chore(deps): update spring boot to v3.2.0`

---

###  Allowed Types

| Type | Description | Example |
| :--- | :--- | :--- |
| `feat` | A new feature for the user | `feat(charts): add real-time vitality spider chart` |
| `fix` | A bug fix | `fix(api): fix null pointer on missing headers` |
| `docs` | Documentation changes only | `docs(api): add OpenAPI endpoint docs` |
| `style` | Formatting, missing semi-colons, no code change | `style(frontend): run prettier on dashboard components` |
| `refactor` | Code restructuring without fixing a bug or adding a feature | `refactor(auth): simplify custom security filter logic` |
| `perf` | Code changes that improve performance | `perf(db): add index to user email lookup` |
| `test` | Adding missing tests or correcting existing tests | `test(auth): add unit tests for token refresh` |
| `chore` | Build process, auxiliary tools, or dependency updates | `chore(gradle): bump dependency versions` |
| `ci` | Changes to CI configuration files and scripts | `ci(github): add workflow for running backend tests` |

---

### Rules to Follow

1. **Imperative Mood:** Use imperative, present tense in the description (*"add"* not *"added"* or *"adds"*).
2. **Lowercase Scope & Type:** Keep types and scopes in lowercase.
3. **No Period:** Do not end the subject line with a period.
4. **Atomic Commits:** Keep commits small and focused on a single change or fix.
