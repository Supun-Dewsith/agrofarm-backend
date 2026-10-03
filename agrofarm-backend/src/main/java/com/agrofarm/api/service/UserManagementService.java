package com.agrofarm.api.service;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UpdateUserRequest;
import com.agrofarm.api.dto.user.UserData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserManagementService {
    List<UserData> getAll();
    UserData updateUser(Long id, UpdateUserRequest request, MultipartFile file);
    AvailableFarmsResponse getAvailableFarms();
    AvailableFarmsResponse getAvailableFarmsByOwnerId(Long ownerId);
}
