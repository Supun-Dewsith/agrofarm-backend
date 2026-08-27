-- store weather data history -  gets weather data daily and store it in this table
CREATE TABLE weather_history(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    farm_id BIGINT NOT NULL ,
    weather_date DATE,
    temperature DOUBLE,
    humidity INT,
    wind_speed DOUBLE,
    rain DOUBLE,
    weather_code INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- store predicted weather for the 10th day from the date that get the weather data.
CREATE TABLE weather_predictions(
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    farm_id BIGINT,
    prediction_date DATE,
    target_date DATE,
    predicted_temperature_max DOUBLE,
    predicted_temperature_min DOUBLE,
    predicted_weather_code INT,
    predicted_uv_index DOUBLE,
    predicted_rain_sum DOUBLE,
    predicted_rain_probability INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- store the actual weather data for the predicted weather data.
CREATE TABLE weather_prediction_results(
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    prediction_id BIGINT NOT NULL ,
    actual_temperature_max DOUBLE,
    actual_temperature_min DOUBLE,
    actual_weather_code INT,
    actual_rain_sum DOUBLE,
    temperature_error DOUBLE,
    weather_match BOOLEAN,
    rain_match BOOLEAN,
    compared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (prediction_id)
        REFERENCES weather_predictions(id)
        ON DELETE CASCADE
);