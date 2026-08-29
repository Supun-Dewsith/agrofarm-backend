CREATE TABLE tasks (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(1000),

                       priority VARCHAR(20) NOT NULL,
                       status VARCHAR(30) NOT NULL,

                       assignee_id BIGINT NOT NULL,
                       field_id BIGINT NOT NULL,

                       due_date DATE NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_task_user
                           FOREIGN KEY (assignee_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE,

                       CONSTRAINT fk_task_field
                           FOREIGN KEY (field_id)
                               REFERENCES fields(id)
                               ON DELETE CASCADE
);