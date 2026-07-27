CREATE TABLE attendance (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            attendance_date DATE NOT NULL,
                            check_in_time TIME,
                            check_out_time TIME,
                            status VARCHAR(20) NOT NULL,
                            remarks VARCHAR(255),

                            CONSTRAINT fk_attendance_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
);