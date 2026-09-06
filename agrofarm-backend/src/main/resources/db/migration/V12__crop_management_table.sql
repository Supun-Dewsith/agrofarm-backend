CREATE TABLE crops(
    crop_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    crop_name VARCHAR(100) NOT NULL ,
    variety VARCHAR(100) ,
    farm_id BIGINT,
    field_id BIGINT,
    growth_stage VARCHAR(50),
    progress INT,
    status VARCHAR(50),
    plant_date TIMESTAMP ,
    harvest_date TIMESTAMP ,
    crop_image_url VARCHAR(500) ,


    CONSTRAINT fk_crop_farm
        FOREIGN KEY (farm_id)
        REFERENCES farms(id),


    CONSTRAINT fk_crop_field
        FOREIGN KEY (field_id)
        REFERENCES fields(id)

);