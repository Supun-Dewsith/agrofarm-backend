package com.agrofarm.api.dto.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long userId,
        String userName,
        String role,
        Long farmId,
        String farmName,
        LocalDate date,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        String status,
        String location,
        Double latitude,
        Double longitude,
        Double accuracyMeters,
        Double checkOutLatitude,
        Double checkOutLongitude,
        Double checkOutAccuracyMeters
) {
}