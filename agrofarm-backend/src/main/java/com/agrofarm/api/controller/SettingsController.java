package com.agrofarm.api.controller;

import com.agrofarm.api.dto.settings.*;
import com.agrofarm.api.exception.SettingsException;
import com.agrofarm.api.security.AuthenticatedUser;
import com.agrofarm.api.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @ExceptionHandler(SettingsException.class)
    public ResponseEntity<Map<String, String>> handleSettingsException(SettingsException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "SETTINGS_ERROR",
                "message", ex.getMessage()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(settingsService.getProfile(user.getId()));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestPart("data") ProfileUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(
                settingsService.updateProfile(user.getId(), request, file)
        );
    }

    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(settingsService.getNotifications(user.getId()));
    }

    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettingsResponse> updateNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody NotificationSettingsRequest request) {
        return ResponseEntity.ok(
                settingsService.updateNotifications(user.getId(), request)
        );
    }

    @GetMapping("/security")
    public ResponseEntity<SecuritySettingsResponse> getSecurity(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(settingsService.getSecurity(user.getId()));
    }

    @PutMapping("/security")
    public ResponseEntity<SecuritySettingsResponse> updateSecurity(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SecuritySettingsRequest request) {
        return ResponseEntity.ok(
                settingsService.updateSecurity(user.getId(), request)
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ChangePasswordRequest request) {
        settingsService.changePassword(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/system")
    public ResponseEntity<SystemSettingsResponse> getSystemSettings(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(settingsService.getSystemSettings(user.getId()));
    }

    @PutMapping("/system")
    public ResponseEntity<SystemSettingsResponse> updateSystemSettings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SystemSettingsRequest request) {
        return ResponseEntity.ok(
                settingsService.updateSystemSettings(user.getId(), request)
        );
    }
}

