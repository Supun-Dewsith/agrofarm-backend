package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.settings.ChangePasswordRequest;
import com.agrofarm.api.dto.settings.NotificationSettingsRequest;
import com.agrofarm.api.dto.settings.NotificationSettingsResponse;
import com.agrofarm.api.dto.settings.ProfileResponse;
import com.agrofarm.api.dto.settings.ProfileUpdateRequest;
import com.agrofarm.api.dto.settings.SecuritySettingsRequest;
import com.agrofarm.api.dto.settings.SecuritySettingsResponse;
import com.agrofarm.api.dto.settings.SystemSettingsRequest;
import com.agrofarm.api.dto.settings.SystemSettingsResponse;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.exception.SettingsException;
import com.agrofarm.api.repository.SettingsRepository;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.SettingsService;
import com.agrofarm.api.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024L * 1024L;

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    // ============================================================
    // PROFILE
    // ============================================================

    @Override
    public ProfileResponse getProfile(Long userId) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        // Check user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        return settingsRepository.getProfile(userId);
    }

    @Override
    public ProfileResponse updateProfile(
            Long userId,
            ProfileUpdateRequest request,
            MultipartFile file
    ) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        if (request == null) {
            throw new SettingsException("Profile data is required");
        }

        UserEntity currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        // ------------------------------------------------------------
        // Validate email
        // ------------------------------------------------------------

        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new SettingsException("Email is required");
        }

        String newEmail = request.email().trim();

        if (!newEmail.contains("@")) {
            throw new SettingsException("Invalid email address");
        }

        // ------------------------------------------------------------
        // Validate profile image
        // ------------------------------------------------------------

        validateProfileImage(file);

        String oldImageUrl = currentUser.getImageUrl();

        String newImageUrl = null;

        try {

            // --------------------------------------------------------
            // Upload new image if supplied
            // --------------------------------------------------------

            if (file != null && !file.isEmpty()) {
                newImageUrl = storageService.uploadFile(file);

                if (newImageUrl == null || newImageUrl.isBlank()) {
                    throw new SettingsException(
                            "Failed to upload profile image"
                    );
                }
            }

            // --------------------------------------------------------
            // Update profile
            // --------------------------------------------------------

            settingsRepository.updateProfile(
                    userId,
                    request,
                    newImageUrl
            );

            // --------------------------------------------------------
            // Delete old image only after successful DB update
            // --------------------------------------------------------

            if (newImageUrl != null
                    && oldImageUrl != null
                    && !oldImageUrl.isBlank()
                    && !oldImageUrl.equals(newImageUrl)) {

                try {
                    storageService.deleteFile(oldImageUrl);
                } catch (RuntimeException ignored) {
                    // Do not fail profile update because old image
                    // deletion failed.
                }
            }

            return settingsRepository.getProfile(userId);

        } catch (DuplicateKeyException ex) {

            // New image was uploaded but DB update failed
            if (newImageUrl != null) {
                try {
                    storageService.deleteFile(newImageUrl);
                } catch (RuntimeException ignored) {
                }
            }

            throw new SettingsException(
                    "Email is already in use"
            );

        } catch (SettingsException ex) {

            // Clean up uploaded image
            if (newImageUrl != null) {
                try {
                    storageService.deleteFile(newImageUrl);
                } catch (RuntimeException ignored) {
                }
            }

            throw ex;

        } catch (RuntimeException ex) {

            // Clean up uploaded image
            if (newImageUrl != null) {
                try {
                    storageService.deleteFile(newImageUrl);
                } catch (RuntimeException ignored) {
                }
            }

            throw ex;
        }
    }

    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    @Override
    public NotificationSettingsResponse getNotifications(Long userId) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        return settingsRepository.getNotifications(userId);
    }

    @Override
    public NotificationSettingsResponse updateNotifications(
            Long userId,
            NotificationSettingsRequest request
    ) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        if (request == null) {
            throw new SettingsException(
                    "Notification settings are required"
            );
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        return settingsRepository.updateNotifications(
                userId,
                request
        );
    }

    // ============================================================
    // SECURITY
    // ============================================================

    @Override
    public SecuritySettingsResponse getSecurity(Long userId) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        return settingsRepository.getSecurity(userId);
    }

    @Override
    public SecuritySettingsResponse updateSecurity(
            Long userId,
            SecuritySettingsRequest request
    ) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        if (request == null) {
            throw new SettingsException(
                    "Security settings are required"
            );
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new SettingsException("User not found"));

        return settingsRepository.updateSecurity(
                userId,
                request
        );
    }

    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Override
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        if (request == null) {
            throw new SettingsException(
                    "Password data is required"
            );
        }

        // ------------------------------------------------------------
        // Validate current password
        // ------------------------------------------------------------

        if (request.currentPassword() == null
                || request.currentPassword().isBlank()) {

            throw new SettingsException(
                    "Current password is required"
            );
        }

        // ------------------------------------------------------------
        // Validate new password
        // ------------------------------------------------------------

        if (request.newPassword() == null
                || request.newPassword().isBlank()) {

            throw new SettingsException(
                    "New password is required"
            );
        }

        // ------------------------------------------------------------
        // Validate confirmation
        // ------------------------------------------------------------

        if (request.confirmNewPassword() == null
                || request.confirmNewPassword().isBlank()) {

            throw new SettingsException(
                    "Password confirmation is required"
            );
        }

        if (!request.newPassword().equals(
                request.confirmNewPassword()
        )) {

            throw new SettingsException(
                    "New password and confirmation do not match"
            );
        }

        // ------------------------------------------------------------
        // Get current user
        // ------------------------------------------------------------

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new SettingsException("User not found")
                );

        // ------------------------------------------------------------
        // Check current password
        // ------------------------------------------------------------

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {

            throw new SettingsException(
                    "Current password is not available"
            );
        }

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {

            throw new SettingsException(
                    "Current password is incorrect"
            );
        }

        // ------------------------------------------------------------
        // New password cannot be same as old password
        // ------------------------------------------------------------

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {

            throw new SettingsException(
                    "New password must be different from current password"
            );
        }

        // ------------------------------------------------------------
        // Save encoded password
        // ------------------------------------------------------------

        String encodedPassword =
                passwordEncoder.encode(
                        request.newPassword()
                );

        settingsRepository.updatePassword(
                userId,
                encodedPassword
        );
    }

    // ============================================================
    // SYSTEM SETTINGS
    // ============================================================

    @Override
    public SystemSettingsResponse getSystemSettings(Long userId) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new SettingsException("User not found")
                );

        return settingsRepository.getSystemSettings(userId);
    }

    @Override
    public SystemSettingsResponse updateSystemSettings(
            Long userId,
            SystemSettingsRequest request
    ) {

        if (userId == null) {
            throw new SettingsException("User ID is required");
        }

        if (request == null) {
            throw new SettingsException(
                    "System settings are required"
            );
        }

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new SettingsException("User not found")
                );

        return settingsRepository.updateSystemSettings(
                userId,
                request
        );
    }

    // ============================================================
    // PROFILE IMAGE VALIDATION
    // ============================================================

    private void validateProfileImage(MultipartFile file) {

        // No image supplied
        if (file == null || file.isEmpty()) {
            return;
        }

        // ------------------------------------------------------------
        // File size
        // ------------------------------------------------------------

        if (file.getSize() > MAX_PROFILE_IMAGE_SIZE) {

            throw new SettingsException(
                    "Profile image must not exceed 5 MB"
            );
        }

        // ------------------------------------------------------------
        // Content type
        // ------------------------------------------------------------

        String contentType = file.getContentType();

        if (contentType == null
                || (!"image/jpeg".equalsIgnoreCase(contentType)
                && !"image/jpg".equalsIgnoreCase(contentType)
                && !"image/png".equalsIgnoreCase(contentType))) {

            throw new SettingsException(
                    "Profile image must be JPG or PNG"
            );
        }

        // ------------------------------------------------------------
        // File extension
        // ------------------------------------------------------------

        String extension =
                StringUtils.getFilenameExtension(
                        file.getOriginalFilename()
                );

        if (extension == null
                || !(extension.equalsIgnoreCase("jpg")
                || extension.equalsIgnoreCase("jpeg")
                || extension.equalsIgnoreCase("png"))) {

            throw new SettingsException(
                    "Profile image must be JPG or PNG"
            );
        }
    }
}