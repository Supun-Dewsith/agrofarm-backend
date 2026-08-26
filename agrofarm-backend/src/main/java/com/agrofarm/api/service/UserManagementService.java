package com.agrofarm.api.service;

import com.agrofarm.api.dto.user.UserData;

import java.util.List;

public interface UserManagementService {
    List<UserData> getAll();
}
