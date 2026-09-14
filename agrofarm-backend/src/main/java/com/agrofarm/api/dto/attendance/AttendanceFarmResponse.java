package com.agrofarm.api.dto.attendance;

public record AttendanceFarmResponse(
        Long id,
        String farmName,
        String farmAddress,
        String boundary
) {}