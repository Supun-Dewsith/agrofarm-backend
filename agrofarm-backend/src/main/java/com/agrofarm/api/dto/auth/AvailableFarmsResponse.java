package com.agrofarm.api.dto.auth;

import java.math.BigDecimal;
import java.util.List;

public record AvailableFarmsResponse(
        List<FarmSummary> farms,
        int totalCount
) {
    public record FarmSummary(
            Long id,
            Long ownerId,
            String farmName,
            String farmAddress,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}
}
