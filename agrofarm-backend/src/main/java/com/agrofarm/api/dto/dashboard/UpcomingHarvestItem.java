package com.agrofarm.api.dto.dashboard;

public record UpcomingHarvestItem(
        Long cropId,
        String cropName,
        String variety,
        String farmName,
        int progress,
        String harvestDate,
        long daysRemaining
) {}
