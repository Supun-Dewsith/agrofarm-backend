package com.agrofarm.api.controller;

import com.agrofarm.api.dto.UserData;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/all")
    public ResponseEntity<?> getWeather() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        UserData logdata = new UserData();
        logdata.setUsername(currentUsername);

        FarmLocation location = weatherService.getLocation(logdata);

        if (location == null) {
            location = new FarmLocation();
            location.setLatitude(6.9271);
            location.setLongitude(79.8612);
        }

        return ResponseEntity.ok(weatherService.getWeather(location.getLatitude(), location.getLongitude()));
    }
}