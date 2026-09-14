package com.agrofarm.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCodeRequest {

    @NotBlank(message = "4-digit attendance code is required")
    @Pattern(
            regexp = "\\d{4}",
            message = "Session code must contain exactly 4 digits"
    )
    private String sessionCode;

    @NotBlank(message = "2-digit live number is required")
    @Pattern(
            regexp = "\\d{2}",
            message = "Live number must contain exactly 2 digits"
    )
    private String rotatingCode;

    @NotBlank(message = "Device type is required")
    @Pattern(
            regexp = "MOBILE|DESKTOP",
            message = "Device type must be MOBILE or DESKTOP"
    )
    private String deviceType;

    private String location;

    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    private Double longitude;

    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Location accuracy must be greater than 0"
    )
    @DecimalMax(
            value = "100.0",
            message = "Location accuracy must be 100 meters or better"
    )
    private Double accuracyMeters;
}
