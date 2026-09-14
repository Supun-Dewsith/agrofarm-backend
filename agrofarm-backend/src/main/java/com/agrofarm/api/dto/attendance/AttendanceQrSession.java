package com.agrofarm.api.dto.attendance;

import java.time.LocalDateTime;

public record AttendanceQrSession(
        Long id,
        String token,
        String manualCode,
        Long farmId,
        Long createdById,
        String createdByName,
        String createdByRole,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        boolean active
) {}