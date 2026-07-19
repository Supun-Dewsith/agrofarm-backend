package com.agrofarm.api.service;


import com.agrofarm.api.dto.CurrentWeather;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.repository.WeatherRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;


@Service
public class SheduledWeatherService {


    private final WeatherService weatherService;
    private final WeatherRepository weatherRepository;

    public SheduledWeatherService(
            WeatherService weatherService,
            WeatherRepository weatherRepository) {
        this.weatherService = weatherService;
        this.weatherRepository = weatherRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void addWeatherToDatabase(){

        System.out.println("Running daily weather updates...");

        List<Long> farmIDs = weatherRepository.getAllFarmIds();

        for (Long farmId : farmIDs){
            FarmLocation location = weatherRepository.getFarmLocationFromFarmid(farmId);

            if (location == null) {
                System.out.println("No location found for farm " + farmId);
                continue;
            }

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            CurrentWeather weather = weatherService.getcurrentWeather(latitude,longitude);
            String dateString = weather.getTime();
            LocalDate date = LocalDate.parse(dateString.substring(0,10));

            double temperature = weather.getTemperature_2m();
            int humidity = weather.getRelative_humidity_2m();
            double windspeed = weather.getWind_speed_10m();
            double rain = weather.getRain();
            int weatherCode = weather.getWeather_code();

            weatherRepository.insertWeatherToWeatherHistoryTable(farmId, date, temperature, humidity, windspeed, rain, weatherCode);


        }
        System.out.println("Daily weather history update complete");
    }
}
