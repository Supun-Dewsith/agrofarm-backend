package com.agrofarm.api.dto;

import lombok.Data;

@Data
public class CurrentWeather {

        private String time;
        private int interval;

        private double temperature_2m;
        private int weather_code;
        private double wind_speed_10m;
        private double rain;
        private int relative_humidity_2m;

        // getters and setters
    
}
