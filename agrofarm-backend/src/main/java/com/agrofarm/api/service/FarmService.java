package com.agrofarm.api.service;

import com.agrofarm.api.dto.FarmRequest;
import com.agrofarm.api.dto.FarmResponse;

import java.util.List;

public interface FarmService {


    FarmResponse createFarm(FarmRequest request);


    List<FarmResponse> getAllFarms();


    FarmResponse getFarmById(Long id);


    FarmResponse updateFarm(
            Long id,
            FarmRequest request
    );


    void deleteFarm(Long id);

}