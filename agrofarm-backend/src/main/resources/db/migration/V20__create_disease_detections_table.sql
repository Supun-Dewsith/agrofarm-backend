CREATE TABLE disease_detections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    plant_name VARCHAR(255) NOT NULL,
    disease_name VARCHAR(255) NOT NULL,
    is_healthy TINYINT(1) NOT NULL DEFAULT 0,
    overall_confidence DOUBLE NOT NULL DEFAULT 0.0,
    general_description TEXT NULL,
    detected_spots TEXT NULL,
    possible_treatments TEXT NULL,
    prevention_tips TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_disease_detections_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    INDEX idx_disease_detections_user_created (user_id, created_at)
);
