package com.agrofarm.api.service;

import com.agrofarm.api.dto.CurrentWeather;
import com.agrofarm.api.dto.UserData;
import com.agrofarm.api.dto.WeatherResponse;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


  // Inject the repositories to the file automaticlly
@Service
public class WeatherService {


        private final WeatherRepository weatherRepository;
        private final RestClient restClient;

        public WeatherService(WeatherRepository weatherRepository) {
            this.weatherRepository = weatherRepository;

            this.restClient = RestClient.builder()
                    .baseUrl("https://api.open-meteo.com")
                    .build();
        }

        public WeatherResponse getWeather(double latitude, double longitude) {

            WeatherResponse response =  restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,rain_sum,precipitation_probability_max")
                            .queryParam("current", "temperature_2m,weather_code,wind_speed_10m,rain,relative_humidity_2m")
                            .queryParam("forecast_days", 11)
                            .build())
                    .retrieve()
                    .body(WeatherResponse.class);
            return response;


        }


        public CurrentWeather getcurrentWeather(double latitude, double longitude){

            WeatherResponse response2 = getWeather(latitude,longitude);
            return response2.getCurrent();

        }


        public FarmLocation getLocation(String username){

            Long user_id = weatherRepository.findDetailsByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            FarmLocation location = weatherRepository.findLatitudeAndLongitudeFromUserID(user_id);
            return location;

        }







}

