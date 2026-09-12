package com.agrofarm.api.service;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UserData;

import java.util.List;

public interface UserManagementService {
    List<UserData> getAll();
    AvailableFarmsResponse getAvailableFarms();
    AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId);
}
