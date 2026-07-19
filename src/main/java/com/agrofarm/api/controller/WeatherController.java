package com.agrofarm.api.controller;

import com.agrofarm.api.dto.CurrentWeather;
import com.agrofarm.api.dto.UserData;
import com.agrofarm.api.dto.WeatherResponse;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.service.WeatherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@CrossOrigin(origins = "http://localhost:5173/", allowCredentials = "true") // frontend port
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private final WeatherService weatherService;


    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getWeather(HttpSession session) {
        FarmLocation location = (FarmLocation) session.getAttribute("userLocation");
        if (location == null) {
            return ResponseEntity.status(401).body("Location not set. Please send user data first.");
        }

        return ResponseEntity.ok(weatherService.getWeather(location.getLatitude(), location.getLongitude()));


    }

   @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(HttpSession session) {
       FarmLocation location = (FarmLocation) session.getAttribute("userLocation");
       if (location == null) {
           return ResponseEntity.status(401).body("Location not set. Please send user data first.");
       }

       return ResponseEntity.ok(weatherService.getcurrentWeather(location.getLatitude(), location.getLongitude()));
   }

}
