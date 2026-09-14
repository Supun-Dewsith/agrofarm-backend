ALTER TABLE attendance
    ADD COLUMN farm_id BIGINT NULL AFTER user_id,
    ADD COLUMN check_in_latitude DECIMAL(10,7) NULL AFTER check_in_time,
    ADD COLUMN check_in_longitude DECIMAL(11,7) NULL AFTER check_in_latitude,
    ADD COLUMN check_out_latitude DECIMAL(10,7) NULL AFTER check_out_time,
    ADD COLUMN check_out_longitude DECIMAL(11,7) NULL AFTER check_out_latitude;

ALTER TABLE attendance
    ADD CONSTRAINT fk_attendance_farm
        FOREIGN KEY (farm_id)
            REFERENCES farms(id)
            ON DELETE CASCADE;

CREATE INDEX idx_attendance_farm
    ON attendance(farm_id);

CREATE INDEX idx_attendance_farm_date
    ON attendance(farm_id, date);

CREATE UNIQUE INDEX uk_attendance_user_farm_date
    ON attendance(user_id, farm_id, date);