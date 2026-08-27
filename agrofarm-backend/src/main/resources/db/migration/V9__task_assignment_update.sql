ALTER TABLE tasks
    ADD COLUMN farm_id BIGINT NOT NULL AFTER status;

ALTER TABLE tasks
    ADD COLUMN supervisor_id BIGINT NULL AFTER assignee_id;

ALTER TABLE tasks
    MODIFY COLUMN field_id BIGINT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_task_farm
        FOREIGN KEY (farm_id)
            REFERENCES farms(id)
            ON DELETE CASCADE;

ALTER TABLE tasks
    ADD CONSTRAINT fk_task_supervisor
        FOREIGN KEY (supervisor_id)
            REFERENCES users(id)
            ON DELETE SET NULL;

ALTER TABLE tasks
    DROP FOREIGN KEY fk_task_field;


ALTER TABLE tasks
    ADD CONSTRAINT fk_task_field
        FOREIGN KEY (field_id)
            REFERENCES fields(id)
            ON DELETE SET NULL;