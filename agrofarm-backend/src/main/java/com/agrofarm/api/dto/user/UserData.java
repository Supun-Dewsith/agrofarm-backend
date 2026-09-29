package com.agrofarm.api.dto.user;

import com.agrofarm.api.util.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record UserData(
        Long id,
        String username,
        LocalDateTime createdAt,
        String email,
        String phoneNumber,
        Role role,
        Boolean enabled,
        String imageUrl,
        Set<String> permissions,
        List<Long> farmIds
) {
    public UserData(
            Long id,
            String username,
            LocalDateTime createdAt,
            String email,
            String phoneNumber,
            Role role,
            Boolean enabled,
            String imageUrl,
            Set<String> permissions
    ) {
        this(id, username, createdAt, email, phoneNumber, role, enabled, imageUrl, permissions, new ArrayList<>());
    }
}