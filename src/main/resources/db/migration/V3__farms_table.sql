CREATE TABLE farms(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL , -- admin user_id from user table
    farm_name VARCHAR(200) NOT NULL,
    farm_address VARCHAR(200) NOT NULL ,
    latitude DECIMAL(10, 7) NOT NULL ,
    longtitude DECIMAL(11, 7) NOT NULL
);
