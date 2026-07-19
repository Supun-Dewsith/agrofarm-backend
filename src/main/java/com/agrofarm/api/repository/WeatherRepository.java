package com.agrofarm.api.repository;

import com.agrofarm.api.entity.FarmLocation;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepository {

        Optional<Long> findDetailsByUsername(String username);
        FarmLocation findLatitudeAndLongitudeFromUserID(Long ID);
        public List<Long> getAllFarmIds();
        public FarmLocation getFarmLocationFromFarmid(Long farmID);
        public void insertWeatherToWeatherHistoryTable(long farm_id, LocalDate date, double temp, int humidity, double wind, double rain, int weather_code );

}
