package com.agrofarm.api.repository;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UserData;

import java.util.List;

public interface UserManagementRepository {
    List<UserData> getAll();
    AvailableFarmsResponse getAvailableFarms();
    AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId);
}
