package com.agrofarm.api.controller;

import com.agrofarm.api.dto.CurrentWeather;
import com.agrofarm.api.dto.WeatherResponse;
import com.agrofarm.api.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true")
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

        // One clean method call!
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
}