package com.agrofarm.api.repository;

import com.agrofarm.api.dto.user.UserData;

import java.util.List;

public interface UserManagementRepository {
    List<UserData> getAll();
}
