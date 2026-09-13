package com.agrofarm.api.controller;

import com.agrofarm.api.dto.weather.CurrentWeather;
import com.agrofarm.api.dto.weather.WeatherResponse;
import com.agrofarm.api.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/all")
    public ResponseEntity<WeatherResponse> getWeather() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        WeatherResponse weather = weatherService.getWeatherForUser(currentUsername);
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentWeather> getCurrentWeather() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        CurrentWeather currentWeather = weatherService.getCurrentWeatherForUser(currentUsername);
        return ResponseEntity.ok(currentWeather);
    }

    @PostMapping("/all")
    public ResponseEntity<WeatherResponse> getallWeather(@RequestBody Long Farmid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        WeatherResponse weather = weatherService.getWeatherForFarm(Farmid);
        return ResponseEntity.ok(weather);
    }
}