CREATE TABLE attendance (

                            id BIGINT NOT NULL AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            user_name VARCHAR(100) NOT NULL,
                            date DATE NOT NULL,
                            check_in_time DATETIME,
                            check_out_time DATETIME,
                            status VARCHAR(20) NOT NULL,
                            location VARCHAR(200),
                            PRIMARY KEY (id),
                            CONSTRAINT fk_attendance_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE
);

CREATE INDEX idx_attendance_user
    ON attendance(user_id);

CREATE INDEX idx_attendance_date
    ON attendance(date);

CREATE INDEX idx_attendance_status
    ON attendance(status);