package com.agrofarm.api.dto.dashboard;

public record FarmOverviewItem(
        Long farmId,
        String farmName,
        String location,
        int fieldsCount,
        int cropsCount,
        int activeTasksCount
) {}
