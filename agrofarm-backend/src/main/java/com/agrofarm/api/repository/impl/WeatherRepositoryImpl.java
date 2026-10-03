package com.agrofarm.api.repository.impl;

import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.entity.FarmLocation;
import com.agrofarm.api.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
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
    public FarmLocation findLatitudeAndLongitudeFromFarmID(Long ID) {
        String sql = "SELECT latitude, longtitude FROM farms WHERE id = ?";
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
}
