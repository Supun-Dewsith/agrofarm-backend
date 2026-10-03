package com.agrofarm.api.repository;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.util.Role;

import java.util.List;

public interface UserManagementRepository {
    List<UserData> getAll();
    List<UserData> getAll(Role callerRole);
    List<UserData> getAll(Role callerRole, Long callerId);
    void updateUser(UserEntity user);
    AvailableFarmsResponse getAvailableFarms();
    AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId);
}
