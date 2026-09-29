package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.settings.*;
import com.agrofarm.api.exception.SettingsException;
import com.agrofarm.api.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SettingsRepositoryImpl implements SettingsRepository {

    private final JdbcTemplate template;

    // ============================================================
    // USER SETTINGS
    // ============================================================

    private void ensureSettings(Long userId) {
        String sql = """
                INSERT IGNORE INTO user_settings (user_id)
                VALUES (?)
                """;

        template.update(sql, userId);
    }

    // ============================================================
    // PROFILE
    // ============================================================

    @Override
    public ProfileResponse getProfile(Long userId) {

        String sql = """
                SELECT id,
                       user_name,
                       first_name,
                       last_name,
                       email,
                       phone_number,
                       image_url,
                       role,
                       enabled,
                       created_at
                FROM users
                WHERE id = ?
                """;

        List<String> permissions = Collections.emptyList();
        try {
            permissions = template.query(
                    """
                    SELECT permission_key
                    FROM user_permissions
                    WHERE user_id = ?
                    ORDER BY permission_key ASC
                    """,
                    (rs, rowNum) -> rs.getString("permission_key"),
                    userId
            );
        } catch (Exception e) {
            log.warn("Failed to load permissions for user {}: {}", userId, e.getMessage());
        }

        List<String> assignedFarms = Collections.emptyList();
        try {
            assignedFarms = template.query(
                    """
                    SELECT f.farm_name
                    FROM farm_assignments fa
                    JOIN farms f ON f.id = fa.farm_id
                    WHERE fa.user_id = ?
                    ORDER BY f.farm_name ASC
                    """,
                    (rs, rowNum) -> rs.getString("farm_name"),
                    userId
            );
        } catch (Exception e) {
            log.warn("Failed to load assigned farms for user {}: {}", userId, e.getMessage());
        }

        final List<String> finalPermissions = permissions;
        final List<String> finalAssignedFarms = assignedFarms;

        try {
            return template.queryForObject(
                    sql,
                    (rs, rowNum) -> {
                        java.sql.Timestamp ts = rs.getTimestamp("created_at");
                        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

                        return new ProfileResponse(
                                rs.getLong("id"),
                                rs.getString("user_name"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("phone_number"),
                                rs.getString("image_url"),
                                rs.getString("role"),
                                rs.getBoolean("enabled"),
                                createdAt,
                                finalPermissions,
                                finalAssignedFarms
                        );
                    },
                    userId
            );
        } catch (EmptyResultDataAccessException ex) {
            throw new SettingsException("User not found");
        }
    }

    @Override
    public void updateProfile(
            Long userId,
            ProfileUpdateRequest request,
            String imageUrl
    ) {

        // ------------------------------------------------------------
        // Update profile WITH new image
        // ------------------------------------------------------------

        if (imageUrl != null && !imageUrl.isBlank()) {

            int updated = template.update(
                    """
                    UPDATE users
                    SET first_name = ?,
                        last_name = ?,
                        email = ?,
                        phone_number = ?,
                        image_url = ?
                    WHERE id = ?
                    """,

                    request.firstName().trim(),
                    normalize(request.lastName()),
                    request.email().trim(),
                    normalize(request.phoneNumber()),
                    imageUrl,
                    userId
            );

            if (updated == 0) {
                throw new SettingsException("User not found");
            }

            return;
        }

        // ------------------------------------------------------------
        // Update profile WITHOUT changing image
        // ------------------------------------------------------------

        int updated = template.update(
                """
                UPDATE users
                SET first_name = ?,
                    last_name = ?,
                    email = ?,
                    phone_number = ?
                WHERE id = ?
                """,

                request.firstName().trim(),
                normalize(request.lastName()),
                request.email().trim(),
                normalize(request.phoneNumber()),
                userId
        );

        if (updated == 0) {
            throw new SettingsException("User not found");
        }
    }

    // ============================================================
    // NOTIFICATIONS
    // ============================================================

    @Override
    public NotificationSettingsResponse getNotifications(Long userId) {

        ensureSettings(userId);

        return template.queryForObject(
                """
                SELECT disease_alerts,
                       low_stock_warnings,
                       task_reminders,
                       harvest_updates,
                       weather_alerts
                FROM user_settings
                WHERE user_id = ?
                """,

                (rs, rowNum) -> new NotificationSettingsResponse(
                        rs.getBoolean("disease_alerts"),
                        rs.getBoolean("low_stock_warnings"),
                        rs.getBoolean("task_reminders"),
                        rs.getBoolean("harvest_updates"),
                        rs.getBoolean("weather_alerts")
                ),

                userId
        );
    }

    @Override
    public NotificationSettingsResponse updateNotifications(
            Long userId,
            NotificationSettingsRequest request
    ) {

        ensureSettings(userId);

        NotificationSettingsResponse current =
                getNotifications(userId);

        boolean diseaseAlerts =
                request.diseaseAlerts() != null
                        ? request.diseaseAlerts()
                        : current.diseaseAlerts();

        boolean lowStockWarnings =
                request.lowStockWarnings() != null
                        ? request.lowStockWarnings()
                        : current.lowStockWarnings();

        boolean taskReminders =
                request.taskReminders() != null
                        ? request.taskReminders()
                        : current.taskReminders();

        boolean harvestUpdates =
                request.harvestUpdates() != null
                        ? request.harvestUpdates()
                        : current.harvestUpdates();

        boolean weatherAlerts =
                request.weatherAlerts() != null
                        ? request.weatherAlerts()
                        : current.weatherAlerts();

        template.update(
                """
                UPDATE user_settings
                SET disease_alerts = ?,
                    low_stock_warnings = ?,
                    task_reminders = ?,
                    harvest_updates = ?,
                    weather_alerts = ?
                WHERE user_id = ?
                """,

                diseaseAlerts,
                lowStockWarnings,
                taskReminders,
                harvestUpdates,
                weatherAlerts,
                userId
        );

        return new NotificationSettingsResponse(
                diseaseAlerts,
                lowStockWarnings,
                taskReminders,
                harvestUpdates,
                weatherAlerts
        );
    }

    // ============================================================
    // SECURITY
    // ============================================================

    @Override
    public SecuritySettingsResponse getSecurity(Long userId) {

        ensureSettings(userId);

        return template.queryForObject(
                """
                SELECT two_factor_authentication
                FROM user_settings
                WHERE user_id = ?
                """,

                (rs, rowNum) ->
                        new SecuritySettingsResponse(
                                rs.getBoolean(
                                        "two_factor_authentication"
                                )
                        ),

                userId
        );
    }

    @Override
    public SecuritySettingsResponse updateSecurity(
            Long userId,
            SecuritySettingsRequest request
    ) {

        ensureSettings(userId);

        SecuritySettingsResponse current =
                getSecurity(userId);

        boolean enabled =
                request.twoFactorAuthentication() != null
                        ? request.twoFactorAuthentication()
                        : current.twoFactorAuthentication();

        template.update(
                """
                UPDATE user_settings
                SET two_factor_authentication = ?
                WHERE user_id = ?
                """,

                enabled,
                userId
        );

        return new SecuritySettingsResponse(enabled);
    }

    // ============================================================
    // SYSTEM SETTINGS
    // ============================================================

    @Override
    public SystemSettingsResponse getSystemSettings(Long userId) {

        ensureSettings(userId);

        return template.queryForObject(
                """
                SELECT farm_name,
                       timezone,
                       currency,
                       auto_backup,
                       ai_disease_detection,
                       weather_sync
                FROM user_settings
                WHERE user_id = ?
                """,

                (rs, rowNum) -> new SystemSettingsResponse(
                        rs.getString("farm_name"),
                        rs.getString("timezone"),
                        rs.getString("currency"),
                        rs.getBoolean("auto_backup"),
                        rs.getBoolean("ai_disease_detection"),
                        rs.getBoolean("weather_sync")
                ),

                userId
        );
    }

    @Override
    public SystemSettingsResponse updateSystemSettings(
            Long userId,
            SystemSettingsRequest request
    ) {

        ensureSettings(userId);

        SystemSettingsResponse current =
                getSystemSettings(userId);

        String farmName =
                request.farmName() != null
                        ? request.farmName().trim()
                        : current.farmName();

        String timezone =
                request.timezone() != null
                        ? request.timezone().trim()
                        : current.timezone();

        String currency =
                request.currency() != null
                        ? request.currency().trim()
                        : current.currency();

        boolean autoBackup =
                request.autoBackup() != null
                        ? request.autoBackup()
                        : current.autoBackup();

        boolean aiDiseaseDetection =
                request.aiDiseaseDetection() != null
                        ? request.aiDiseaseDetection()
                        : current.aiDiseaseDetection();

        boolean weatherSync =
                request.weatherSync() != null
                        ? request.weatherSync()
                        : current.weatherSync();

        template.update(
                """
                UPDATE user_settings
                SET farm_name = ?,
                    timezone = ?,
                    currency = ?,
                    auto_backup = ?,
                    ai_disease_detection = ?,
                    weather_sync = ?
                WHERE user_id = ?
                """,

                farmName,
                timezone,
                currency,
                autoBackup,
                aiDiseaseDetection,
                weatherSync,
                userId
        );

        return new SystemSettingsResponse(
                farmName,
                timezone,
                currency,
                autoBackup,
                aiDiseaseDetection,
                weatherSync
        );
    }

    // ============================================================
    // PASSWORD
    // ============================================================

    @Override
    public void updatePassword(
            Long userId,
            String encodedPassword
    ) {

        int updated = template.update(
                """
                UPDATE users
                SET password = ?
                WHERE id = ?
                """,

                encodedPassword,
                userId
        );

        if (updated == 0) {
            throw new SettingsException("User not found");
        }
    }

    // ============================================================
    // NORMALIZE
    // ============================================================

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
