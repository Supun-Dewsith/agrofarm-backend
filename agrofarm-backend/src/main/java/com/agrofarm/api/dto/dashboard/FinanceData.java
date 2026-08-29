package com.agrofarm.api.dto.dashboard;

public record FinanceData(
        String month,
        Double revenue,
        Double expense
) {}