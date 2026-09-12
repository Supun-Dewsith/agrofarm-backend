package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.repository.UserManagementRepository;
import com.agrofarm.api.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    private final UserManagementRepository userManagementRepository;

    @Override
    public List<UserData> getAll() {
        return userManagementRepository.getAll();
    }

    @Override
    public AvailableFarmsResponse getAvailableFarms() {
        return userManagementRepository.getAvailableFarms();
    }

    @Override
    public AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId) {
        return userManagementRepository.getAvailableFarmsByOwnerId(ownerId);
    }

}
