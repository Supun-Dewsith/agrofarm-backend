package com.agrofarm.api.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {
    // Financial KPIs
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;
    private Double profitMargin;

    // Operational KPIs
    private Long activeCropsCount;
    private Long healthyCropsCount;
    private Long completedTasksCount;
    private Long pendingTasksCount;
    private Long totalAttendanceCount;
    private Long diseaseAlertsCount;

    // Charts data
    private List<Map<String, Object>> financialTrend;
    private List<Map<String, Object>> cropStatusDistribution;
    private List<Map<String, Object>> taskPriorityDistribution;
}
