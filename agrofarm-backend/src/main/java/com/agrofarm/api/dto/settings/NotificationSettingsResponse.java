package com.agrofarm.api.dto.settings;

public record NotificationSettingsResponse(
        boolean diseaseAlerts,
        boolean lowStockWarnings,
        boolean taskReminders,
        boolean harvestUpdates,
        boolean weatherAlerts
) {}
