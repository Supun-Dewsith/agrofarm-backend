package com.agrofarm.api.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialTransactionDto {
    private Long id;
    private Long farmId;
    private String farmName;
    private Long cropId;
    private String cropName;
    private String recordType; // INCOME, EXPENSE
    private String category;   // FERTILIZER, SEEDS, LABOR_WAGES, CROP_SALES, etc.
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private String recordedByName;
}
