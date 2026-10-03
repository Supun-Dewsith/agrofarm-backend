-- Migration V23: Add comprehensive biological profile, yield forecast, and IPM matrix fields to disease_detections
ALTER TABLE disease_detections
    ADD COLUMN farm_id BIGINT NULL,
    ADD COLUMN field_id BIGINT NULL,
    ADD COLUMN pathogen_type VARCHAR(100) NULL,
    ADD COLUMN scientific_name VARCHAR(255) NULL,
    ADD COLUMN optimal_temperature VARCHAR(100) NULL,
    ADD COLUMN humidity_level VARCHAR(100) NULL,
    ADD COLUMN weather_correlation TEXT NULL,
    ADD COLUMN life_cycle_stages TEXT NULL,
    ADD COLUMN yield_loss_risk VARCHAR(255) NULL,
    ADD COLUMN yield_loss_percentage INT NULL DEFAULT 0,
    ADD COLUMN spread_speed VARCHAR(255) NULL,
    ADD COLUMN harvest_impact TEXT NULL,
    ADD COLUMN ipm_matrix TEXT NULL,
    ADD COLUMN probability_distribution TEXT NULL,
    ADD COLUMN reference_image_url VARCHAR(1000) NULL;

CREATE INDEX idx_disease_detections_farm_field ON disease_detections (farm_id, field_id);
