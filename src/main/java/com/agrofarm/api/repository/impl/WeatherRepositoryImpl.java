package com.agrofarm.api.repository.impl;

import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeatherRepositoryImpl implements WeatherRepository {
    private final JdbcTemplate template;

    @Override
    public Optional<Long> findDetailsByUsername(String username) {
        String sql = "SELECT id FROM users WHERE user_name = ?";
        try {
            // Use queryForObject to return a single Long value
            Long id = template.queryForObject(sql, Long.class, username);
            return Optional.ofNullable(id);
        } catch (EmptyResultDataAccessException e) {
            // Return empty if no user is found with that username
            return Optional.empty();
        }
    }
    @Override
    public FarmLocation findLatitudeAndLongitudeFromUserID(Long ID) {
        String sql = "SELECT latitude, longtitude FROM farms WHERE owner_id = ?";
        try {
            return template.queryForObject(sql, (rs, rowNum) -> {
                FarmLocation location = new FarmLocation();
                location.setLatitude(rs.getDouble("latitude"));
                location.setLongitude(rs.getDouble("longtitude"));
                return location;
            }, ID);

        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }
    }




    @Override
    public List<Long> getAllFarmIds(){
        String sql = "SELECT id FROM farms";
        return template.queryForList(sql, Long.class);
    }


    @Override
    public FarmLocation getFarmLocationFromFarmid(Long farmID){
        String sql = "SELECT latitude, longtitude FROM farms WHERE id = ?";

        try {
            return template.queryForObject(sql, (rs, rowNum) -> {
                FarmLocation location = new FarmLocation();
                location.setLatitude(rs.getDouble("latitude"));
                location.setLongitude(rs.getDouble("longtitude"));
                return location;
            }, farmID);

        } catch (EmptyResultDataAccessException e) {
            // Return null or handle the case where no farm is found for this user
            return null;
        }
    }




    @Override
    public void insertWeatherToWeatherHistoryTable(long farm_id, LocalDate date, double temp, int humidity, double wind, double rain, int weather_code ){

        String sql = """
                INSERT INTO weather_history
                (farm_id, weather_date, temperature, humidity, wind_speed, rain, weather_code)
                VALUES (?,?,?,?,?,?,?)
                """;

        template.update(
                sql,
                farm_id,
                Date.valueOf(date),
                temp,
                humidity,
                wind,
                rain,
                weather_code
        );

    }
}
