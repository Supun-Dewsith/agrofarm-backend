package com.agrofarm.api.service;

import com.agrofarm.api.dto.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

        private final RestClient restClient;

        public WeatherService() {
            this.restClient = RestClient.builder()
                    .baseUrl("https://api.open-meteo.com")
                    .build();
        }

        public WeatherResponse getWeather(double latitude, double longitude) {

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,rain_sum,precipitation_probability_max")
                            .queryParam("current", "temperature_2m,weather_code,wind_speed_10m,rain,relative_humidity_2m")
                            .queryParam("forecast_days", 16)
                            .build())
                    .retrieve()
                    .body(WeatherResponse.class);
        }
    }

