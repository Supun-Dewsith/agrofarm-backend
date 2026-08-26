package com.agrofarm.api.dto.dashboard;

public record StatCardProp(
        String title,
        Object value,
        String change,
        Boolean isPositive,
        String type
) {}