package com.agrofarm.api.dto;

import java.time.LocalDateTime;

public record QrAttendanceResponse(

        boolean success,

        String message,

        LocalDateTime checkInTime,

        LocalDateTime checkOutTime,

        String status,

        Double latitude,

        Double longitude,

        Double accuracyMeters

) {
}