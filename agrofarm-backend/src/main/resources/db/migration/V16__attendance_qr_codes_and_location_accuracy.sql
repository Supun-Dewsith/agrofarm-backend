ALTER TABLE attendance
    ADD COLUMN check_in_accuracy_meters DECIMAL(10,2) NULL AFTER check_in_longitude,
    ADD COLUMN check_out_accuracy_meters DECIMAL(10,2) NULL AFTER check_out_longitude;

CREATE TABLE attendance_qr_tokens (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      token VARCHAR(100) NOT NULL,
                                      manual_code CHAR(4) NOT NULL,
                                      farm_id BIGINT NOT NULL,
                                      created_by BIGINT NOT NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      expires_at DATETIME NOT NULL,
                                      active TINYINT(1) NOT NULL DEFAULT 1,
                                      PRIMARY KEY (id),
                                      CONSTRAINT uk_attendance_qr_token UNIQUE (token),
                                      CONSTRAINT fk_attendance_qr_farm
                                          FOREIGN KEY (farm_id)
                                              REFERENCES farms(id)
                                              ON DELETE CASCADE,
                                      CONSTRAINT fk_attendance_qr_creator
                                          FOREIGN KEY (created_by)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE
);

CREATE INDEX idx_attendance_qr_farm
    ON attendance_qr_tokens(farm_id);

CREATE INDEX idx_attendance_qr_expiry
    ON attendance_qr_tokens(expires_at, active);

CREATE INDEX idx_attendance_qr_manual_code
    ON attendance_qr_tokens(manual_code, active, expires_at);