package com.agrofarm.api.service;

import com.agrofarm.api.dto.weather.CurrentWeather;
import com.agrofarm.api.dto.weather.WeatherResponse;

public interface WeatherService {


    public WeatherResponse getWeatherForUser(String username);
    public CurrentWeather getCurrentWeatherForUser(String username);

    private WeatherResponse fetchWeatherFromApi(double latitude, double longitude) {
        return null;
    }

}
