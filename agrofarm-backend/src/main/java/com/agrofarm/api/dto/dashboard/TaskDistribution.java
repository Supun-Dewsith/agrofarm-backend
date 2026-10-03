package com.agrofarm.api.dto.dashboard;

public record TaskDistribution(
        long total,
        long pending,
        long inProgress,
        long completed,
        long highPriority,
        long mediumPriority,
        long lowPriority,
        double completionRate
) {}
