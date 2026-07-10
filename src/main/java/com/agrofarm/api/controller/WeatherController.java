package com.agrofarm.api.controller;

import com.agrofarm.api.dto.CurrentWeather;
import com.agrofarm.api.dto.WeatherResponse;
import com.agrofarm.api.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/all")
    public WeatherResponse getWeather() {

        return weatherService.getWeather(6.854356,80.092613);


    }

   @GetMapping("/current")
    public CurrentWeather getCurrentWeather() {
        return weatherService.getcurrentWeather(6.854356,80.092613);
   }

}
