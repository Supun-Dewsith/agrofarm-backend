package com.agrofarm.api.repository;

import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.entity.FarmLocation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeatherRepository {

        Optional<Long> findDetailsByUsername(String username);
        FarmLocation findLatitudeAndLongitudeFromUserID(Long ID);

}
