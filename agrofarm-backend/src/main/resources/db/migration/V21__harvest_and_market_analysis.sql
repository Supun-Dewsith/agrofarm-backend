CREATE TABLE harvest_records (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 crop_id BIGINT NOT NULL,
                                 harvested_on DATE NOT NULL,
                                 quantity_kg DECIMAL(12,3) NOT NULL,
                                 grade VARCHAR(30) NULL,
                                 unit_price_lkr_per_kg DECIMAL(12,2) NULL,
                                 sold_market VARCHAR(150) NULL,
                                 notes VARCHAR(1000) NULL,
                                 created_by BIGINT NOT NULL,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_harvest_crop
                                     FOREIGN KEY (crop_id)
                                         REFERENCES crops(crop_id),

                                 CONSTRAINT fk_harvest_user
                                     FOREIGN KEY (created_by)
                                         REFERENCES users(id),

                                 CONSTRAINT chk_harvest_quantity_positive
                                     CHECK (quantity_kg > 0),

                                 INDEX idx_harvest_crop_date (crop_id, harvested_on),
                                 INDEX idx_harvest_created_by (created_by)
);

CREATE TABLE harvest_market_prices (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       commodity_name VARCHAR(100) NOT NULL,
                                       market_name VARCHAR(150) NOT NULL,
                                       observed_on DATE NOT NULL,
                                       price_lkr_per_kg DECIMAL(12,2) NOT NULL,
                                       price_type VARCHAR(30) NOT NULL DEFAULT 'WHOLESALE',
                                       source_name VARCHAR(120) NOT NULL,
                                       source_url VARCHAR(500) NULL,
                                       market_latitude DECIMAL(10,7) NULL,
                                       market_longitude DECIMAL(11,7) NULL,
                                       entered_by BIGINT NOT NULL,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_market_price_user
                                           FOREIGN KEY (entered_by)
                                               REFERENCES users(id),

                                       CONSTRAINT chk_market_price_positive
                                           CHECK (price_lkr_per_kg > 0),

                                       INDEX idx_market_commodity_date (commodity_name, observed_on),
                                       INDEX idx_market_location (market_latitude, market_longitude)
);