package com.agrofarm.api.dto.settings;

public record SecuritySettingsRequest(
        Boolean twoFactorAuthentication
) {}