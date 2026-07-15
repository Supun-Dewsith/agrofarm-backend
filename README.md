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