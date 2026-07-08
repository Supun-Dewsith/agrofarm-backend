CREATE TABLE farms(
    id BIGINT AUTO_INCREMET PRIMARY KEY,
    owner_id BIGINT NOT NULL ,
    farm_name VARCHAR(200) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL ,
    longtitude DECIMAL(11, 7) NOT NULL
);
