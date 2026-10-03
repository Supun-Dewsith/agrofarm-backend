package com.agrofarm.api.dto.dashboard;

public record ExpenseCategoryBreakdown(
        String category,
        double amount,
        int percentage,
        String color
) {}
