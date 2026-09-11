package com.agrofarm.api.dto.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemSettingsRequest(
        @NotBlank(message = "Farm name is required")
        @Size(max = 200, message = "Farm name must not exceed 200 characters")
        String farmName,

        @NotBlank(message = "Timezone is required")
        @Size(max = 100, message = "Timezone must not exceed 100 characters")
        String timezone,

        @NotBlank(message = "Currency is required")
        @Size(max = 20, message = "Currency must not exceed 20 characters")
        String currency,

        Boolean autoBackup,
        Boolean aiDiseaseDetection,
        Boolean weatherSync
) {}
