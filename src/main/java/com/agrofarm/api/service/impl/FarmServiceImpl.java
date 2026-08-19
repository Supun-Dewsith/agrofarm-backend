package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.FarmRequest;
import com.agrofarm.api.dto.FarmResponse;
import com.agrofarm.api.entity.Farm;
import com.agrofarm.api.repository.FarmRepository;
import com.agrofarm.api.service.FarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;

    /* =========================================================
       CREATE FARM
    ========================================================= */

    @Override
    public FarmResponse createFarm(FarmRequest request) {

        Farm farm = Farm.builder()
                .ownerId(request.getOwnerId())
                .farmName(request.getFarmName())
                .farmAddress(request.getFarmAddress())
                .latitude(request.getLatitude())
                .longtitude(request.getLongtitude())
                .boundary(request.getBoundary())
                .build();

        Farm savedFarm = farmRepository.save(farm);

        return mapToResponse(savedFarm);
    }

    /* =========================================================
       GET ALL FARMS
    ========================================================= */

    @Override
    public List<FarmResponse> getAllFarms() {

        return farmRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /* =========================================================
       GET FARM BY ID
    ========================================================= */

    @Override
    public FarmResponse getFarmById(Long id) {

        Farm farm = farmRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : " + id
                        )
                );

        return mapToResponse(farm);
    }

    /* =========================================================
       GET FARMS BY OWNER
    ========================================================= */

    @Override
    public List<FarmResponse> getFarmsByOwner(Long ownerId) {

        return farmRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /* =========================================================
       UPDATE FARM
    ========================================================= */

    @Override
    public FarmResponse updateFarm(
            Long id,
            FarmRequest request
    ) {

        Farm farm = farmRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : " + id
                        )
                );

        farm.setOwnerId(request.getOwnerId());
        farm.setFarmName(request.getFarmName());
        farm.setFarmAddress(request.getFarmAddress());
        farm.setLatitude(request.getLatitude());
        farm.setLongtitude(request.getLongtitude());
        farm.setBoundary(request.getBoundary());

        Farm updatedFarm = farmRepository.save(farm);

        return mapToResponse(updatedFarm);
    }

    /* =========================================================
       DELETE FARM
    ========================================================= */

    @Override
    public void deleteFarm(Long id) {

        Farm farm = farmRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Farm not found with id : " + id
                        )
                );

        farmRepository.delete(farm);
    }



    private FarmResponse mapToResponse(Farm farm) {

        return FarmResponse.builder()
                .id(farm.getId())
                .ownerId(farm.getOwnerId())
                .farmName(farm.getFarmName())
                .farmAddress(farm.getFarmAddress())
                .latitude(farm.getLatitude())
                .longtitude(farm.getLongtitude())
                .boundary(farm.getBoundary())
                .build();
    }
}