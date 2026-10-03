package com.agrofarm.api.dto.attendance;

import java.time.LocalDateTime;

public record QrLiveCodeResponse(
        String rotatingCode,
        int rotationSeconds,
        int secondsRemaining,
        LocalDateTime generatedAt,
        LocalDateTime rotatingCodeExpiresAt,
        LocalDateTime sessionExpiresAt
) {}

