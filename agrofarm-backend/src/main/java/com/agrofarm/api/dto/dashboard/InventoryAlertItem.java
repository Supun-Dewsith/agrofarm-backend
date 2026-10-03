package com.agrofarm.api.dto.dashboard;

public record InventoryAlertItem(
        Long id,
        String name,
        String category,
        Double quantity,
        String unit,
        Double alertLimit,
        String farmName,
        String status
) {}
