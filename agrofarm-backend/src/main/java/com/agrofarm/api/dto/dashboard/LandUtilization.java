package com.agrofarm.api.dto.dashboard;

import java.util.Map;

public record LandUtilization(
        double totalAreaAcres,
        int totalFields,
        Map<String, Integer> soilDistribution
) {}
