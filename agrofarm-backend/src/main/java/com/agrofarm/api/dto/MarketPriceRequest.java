package com.agrofarm.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketPriceRequest(
        Long cropId,
        String marketName,
        LocalDate observedOn,
        BigDecimal priceLkrPerKg,
        String priceType,
        String sourceName,
        String sourceUrl,
        BigDecimal marketLatitude,
        BigDecimal marketLongitude
) {
}
