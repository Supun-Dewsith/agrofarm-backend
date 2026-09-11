package com.agrofarm.api.service;

import com.agrofarm.api.dto.settings.*;
import org.springframework.web.multipart.MultipartFile;

public interface SettingsService {
    ProfileResponse getProfile(Long userId);
    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request, MultipartFile file);

    NotificationSettingsResponse getNotifications(Long userId);
    NotificationSettingsResponse updateNotifications(Long userId, NotificationSettingsRequest request);

    SecuritySettingsResponse getSecurity(Long userId);
    SecuritySettingsResponse updateSecurity(Long userId, SecuritySettingsRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);

    SystemSettingsResponse getSystemSettings(Long userId);
    SystemSettingsResponse updateSystemSettings(Long userId, SystemSettingsRequest request);
}