package com.agrofarm.api.dto.attendance;

public record AttendanceWorkerResponse(
        Long id,
        String userName,
        String email,
        String phoneNumber,
        String role,
        Long farmId,
        String farmName
) {}

