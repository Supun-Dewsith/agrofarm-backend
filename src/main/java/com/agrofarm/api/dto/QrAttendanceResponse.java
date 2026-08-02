package com.agrofarm.api.dto;

import java.time.LocalDateTime;

public record QrAttendanceResponse(

        boolean success,

        String message,

        LocalDateTime checkInTime

) {
}