package com.agrofarm.api.dto.settings;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String imageUrl,
        String role,
        Boolean enabled,
        LocalDateTime createdAt,
        List<String> permissions,
        List<String> assignedFarms
) {
    public ProfileResponse(
            Long id,
            String username,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String imageUrl
    ) {
        this(id, username, firstName, lastName, email, phoneNumber, imageUrl, null, true, null, List.of(), List.of());
    }
}
