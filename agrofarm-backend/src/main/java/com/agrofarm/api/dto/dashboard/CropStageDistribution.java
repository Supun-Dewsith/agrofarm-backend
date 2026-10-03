package com.agrofarm.api.dto.dashboard;

public record CropStageDistribution(
        String stage,
        int count,
        int percentage,
        String color
) {}
