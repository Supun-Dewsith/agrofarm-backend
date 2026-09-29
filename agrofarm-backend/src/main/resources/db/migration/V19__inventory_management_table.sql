CREATE TABLE inventory (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           inventory_name VARCHAR(100) NOT NULL,
                           category VARCHAR(100) NOT NULL,
                           quantity VARCHAR(50) NOT NULL,
                           stock_level_percent INT,
                           last_restocked DATE,
                           farm_id BIGINT NOT NULL,

                           CONSTRAINT fk_inventory_farm
                               FOREIGN KEY (farm_id)
                                   REFERENCES farms(id)
                                   ON DELETE CASCADE
);