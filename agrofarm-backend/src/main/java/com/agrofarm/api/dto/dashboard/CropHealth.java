package com.agrofarm.api.dto.dashboard;

public record CropHealth(
        String name,
        Integer value,
        String color
) {}