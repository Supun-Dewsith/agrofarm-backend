package com.agrofarm.api.dto.settings;

public record NotificationSettingsRequest(
        Boolean diseaseAlerts,
        Boolean lowStockWarnings,
        Boolean taskReminders,
        Boolean harvestUpdates,
        Boolean weatherAlerts
) {}

