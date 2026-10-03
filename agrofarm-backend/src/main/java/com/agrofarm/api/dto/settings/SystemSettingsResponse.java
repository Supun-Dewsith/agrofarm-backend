package com.agrofarm.api.dto.settings;

public record SystemSettingsResponse(
        String farmName,
        String timezone,
        String currency,
        boolean autoBackup,
        boolean aiDiseaseDetection,
        boolean weatherSync
) {}