CREATE TABLE farm_assignments (
    user_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, farm_id),
    CONSTRAINT fk_fa_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fa_farm FOREIGN KEY (farm_id) REFERENCES farms(id) ON DELETE CASCADE
);

CREATE INDEX idx_fa_farm_id ON farm_assignments(farm_id);