package com.agrofarm.api.dto.dashboard;

public record HarvestSummary(
        String crop,
        Integer actual,
        Integer target
) {}