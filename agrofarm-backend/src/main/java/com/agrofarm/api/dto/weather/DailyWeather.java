package com.agrofarm.api.dto.weather;

import lombok.Data;

import java.util.List;

@Data
public class DailyWeather {

        private List<String> time;
        private List<Integer> weather_code;
        private List<Double> temperature_2m_max;
        private List<Double> temperature_2m_min;
        private List<Double> uv_index_max;
        private List<Double> rain_sum;
        private List<Integer> precipitation_probability_max;

        // getters and setters
    
}
