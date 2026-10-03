package com.agrofarm.api.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt
) {
}