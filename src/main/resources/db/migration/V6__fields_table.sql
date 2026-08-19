CREATE TABLE fields (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        farm_id BIGINT NOT NULL,
                        field_name VARCHAR(100) NOT NULL,
                        field_size DECIMAL(10,2) NOT NULL,
                        size_unit ENUM('ACRE','HECTARE','SQ_METER') DEFAULT 'ACRE',
                        latitude DECIMAL(10,7) NOT NULL,
                        longtitude DECIMAL(11,7) NOT NULL,

                        CONSTRAINT fk_fields_farm
                            FOREIGN KEY (farm_id)
                                REFERENCES farms(id)
                                ON DELETE CASCADE
);