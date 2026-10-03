package com.agrofarm.api.dto.attendance;

import java.time.LocalDateTime;

public record QrGenerateResponse(
        String token,
        Long farmId,
        String farmName,
        String manualCode,
        String rotatingCode,
        int rotationSeconds,
        int rotatingCodeSecondsRemaining,
        LocalDateTime rotatingCodeExpiresAt,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Long createdById,
        String createdByName,
        String createdByRole
) {}
