package com.agrofarm.api.dto.user;

import com.agrofarm.api.util.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserData(
        Long id,
        String username,
        LocalDateTime createdAt,
        String email,
        String phoneNumber,
        Role role,
        Boolean enabled,
        Set<String> permissions
) {
}