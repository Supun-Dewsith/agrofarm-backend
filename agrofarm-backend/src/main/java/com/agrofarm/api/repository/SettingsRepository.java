package com.agrofarm.api.repository;

import com.agrofarm.api.dto.settings.*;

public interface SettingsRepository {
    ProfileResponse getProfile(Long userId);
    void updateProfile(Long userId, ProfileUpdateRequest request, String imageUrl);

    NotificationSettingsResponse getNotifications(Long userId);
    NotificationSettingsResponse updateNotifications(Long userId, NotificationSettingsRequest request);

    SecuritySettingsResponse getSecurity(Long userId);
    SecuritySettingsResponse updateSecurity(Long userId, SecuritySettingsRequest request);

    SystemSettingsResponse getSystemSettings(Long userId);
    SystemSettingsResponse updateSystemSettings(Long userId, SystemSettingsRequest request);

    void updatePassword(Long userId, String encodedPassword);
}
