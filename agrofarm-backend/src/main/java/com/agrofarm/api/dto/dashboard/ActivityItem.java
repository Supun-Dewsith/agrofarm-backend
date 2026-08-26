package com.agrofarm.api.dto.dashboard;

public record ActivityItem(
        String id,
        String type,
        String title,
        String description,
        String time
) {}