package com.agrofarm.api.dto.settings;

public record ProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String imageUrl
) {}
