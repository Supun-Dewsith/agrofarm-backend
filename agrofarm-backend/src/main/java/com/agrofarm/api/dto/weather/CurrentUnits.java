package com.agrofarm.api.dto.weather;

import lombok.Data;

@Data
public class CurrentUnits {
    private String time;
    private String interval;
    private String temperature_2m;
    private String weather_code;
    private String wind_speed_10m;
    private String rain;
    private String relative_humidity_2m;
}
