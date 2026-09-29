CREATE TABLE IF NOT EXISTS farm_financial_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NULL,
    record_type VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description VARCHAR(255),
    recorded_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_financial_farm
        FOREIGN KEY (farm_id)
        REFERENCES farms(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_financial_crop
        FOREIGN KEY (crop_id)
        REFERENCES crops(crop_id)
        ON DELETE SET NULL
);

CREATE INDEX idx_financial_farm_date ON farm_financial_records(farm_id, transaction_date);
CREATE INDEX idx_financial_type ON farm_financial_records(record_type);
