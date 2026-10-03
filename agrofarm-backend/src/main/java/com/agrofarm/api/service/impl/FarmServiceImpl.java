package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.FarmRequest;
import com.agrofarm.api.dto.FarmResponse;
import com.agrofarm.api.entity.Farm;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.FarmRepository;
import com.agrofarm.api.service.FarmAccessService;
import com.agrofarm.api.service.FarmService;
import com.agrofarm.api.util.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {

    private final FarmRepository farmRepository;
    private final FarmAccessService farmAccessService;

    @Override
    public FarmResponse createFarm(FarmRequest request) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireAdmin(user);

        Farm farm = new Farm();

        // Automatically assign the logged-in administrator.
        farm.setOwnerId(user.getId());

        applyRequest(farm, request);

        Farm savedFarm = farmRepository.save(farm);

        return mapToResponse(savedFarm);
    }

    @Override
    public List<FarmResponse> getAllFarms() {

        UserEntity user = farmAccessService.currentUser();

        List<Farm> farms;

        if (user.getRole() == Role.ADMIN) {
            farms = farmRepository.findAll();
        } else {
            farms = farmRepository.findAssignedToUser(user.getId());
        }

        return farms.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public FarmResponse getFarmById(Long id) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireRead(user, id);

        return mapToResponse(findFarm(id));
    }

    @Override
    public FarmResponse updateFarm(
            Long id,
            FarmRequest request
    ) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireAdmin(user);

        Farm farm = findFarm(id);

        // Updating the farm does not change its original owner.
        applyRequest(farm, request);

        Farm updatedFarm = farmRepository.save(farm);

        return mapToResponse(updatedFarm);
    }

    @Override
    public void deleteFarm(Long id) {

        UserEntity user = farmAccessService.currentUser();

        farmAccessService.requireAdmin(user);

        Farm farm = findFarm(id);

        farmRepository.delete(farm);
    }

    private Farm findFarm(Long id) {

        return farmRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Farm not found"
                        )
                );
    }

    private void applyRequest(
            Farm farm,
            FarmRequest request
    ) {

        farm.setFarmName(request.getFarmName());
        farm.setFarmAddress(request.getFarmAddress());
        farm.setLatitude(request.getLatitude());
        farm.setLongtitude(request.getLongtitude());
        farm.setBoundary(request.getBoundary());
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