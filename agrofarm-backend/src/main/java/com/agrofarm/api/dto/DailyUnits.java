package com.agrofarm.api.dto;

import lombok.Data;

@Data
public class DailyUnits {
    private String time;
    private String weather_code;
    private String temperature_2m_max;
    private String temperature_2m_min;
    private String uv_index_max;
    private String rain_sum;
    private String precipitation_probability_max;
}
