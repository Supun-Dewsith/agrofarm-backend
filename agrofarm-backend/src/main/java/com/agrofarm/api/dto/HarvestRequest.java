package com.agrofarm.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HarvestRequest(
        Long cropId,
        LocalDate harvestedOn,
        BigDecimal quantityKg,
        String grade,
        BigDecimal unitPriceLkrPerKg,
        String soldMarket,
        String notes
) {
}
