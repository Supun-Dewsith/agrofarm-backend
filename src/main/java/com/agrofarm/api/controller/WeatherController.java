package com.agrofarm.api.controller;

import com.agrofarm.api.dto.WeatherResponse;
import com.agrofarm.api.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponse getWeather() {

        return weatherService.getWeather(7.75, 80.75);

    }
}
