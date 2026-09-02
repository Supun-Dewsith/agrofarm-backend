package com.agrofarm.api.service;

import com.agrofarm.api.dto.weather.CurrentWeather;
import com.agrofarm.api.dto.weather.WeatherResponse;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.exception.LocationNotFoundException;
import com.agrofarm.api.repository.WeatherRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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

    // Public method used by controller
    public WeatherResponse getWeatherForUser(String username) {
        FarmLocation location = getLocation(username); // Throws LocationNotFoundException if missing
        return fetchWeatherFromApi(location.getLatitude(), location.getLongitude());
    }

    public CurrentWeather getCurrentWeatherForUser(String username) {
        WeatherResponse response = getWeatherForUser(username);
        return response.getCurrent();
    }

    // External Open-Meteo API Call
    private WeatherResponse fetchWeatherFromApi(double latitude, double longitude) {
        return restClient.get()
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
    }

    // Private location helper check
    private FarmLocation getLocation(String username) {
        Long userId = weatherRepository.findDetailsByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FarmLocation location = weatherRepository.findLatitudeAndLongitudeFromUserID(userId);

        if (location == null || location.getLatitude() == 0 || location.getLongitude() == 0) {
            throw new LocationNotFoundException("Location data is empty. Please configure your farm location.");
        }

        return location;
    }
}