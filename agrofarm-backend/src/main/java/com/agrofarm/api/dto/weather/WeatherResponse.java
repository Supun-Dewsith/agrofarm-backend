package com.agrofarm.api.dto.weather;

import lombok.Data;

@Data
public class WeatherResponse {

        private double latitude;
        private double longitude;
        private double generationtime_ms;
        private int utc_offset_seconds;
        private String timezone;
        private String timezone_abbreviation;
        private double elavation;

        private CurrentUnits current_units;
        private CurrentWeather current;

        private DailyUnits daily_units;
        private DailyWeather daily;

        // getters and setters

}
