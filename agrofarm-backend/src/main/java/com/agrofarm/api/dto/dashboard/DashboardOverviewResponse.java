package com.agrofarm.api.dto.dashboard;

import java.util.List;

public record DashboardOverviewResponse(
        List<StatCardProp> stats,
        Weather weather,
        List<CropHealth> cropHealth,
        List<FinanceData> financeData,
        List<HarvestSummary> harvestSummary,
        List<ActivityItem> recentActivities,
        TaskDistribution taskDistribution,
        List<InventoryAlertItem> lowStockAlerts,
        List<FarmOverviewItem> farmOverviews,
        List<CropStageDistribution> cropStages,
        List<UpcomingHarvestItem> upcomingHarvests,
        List<ExpenseCategoryBreakdown> expenseBreakdown,
        List<AttendanceTrendDay> attendanceTrend,
        List<DiseasePrevalenceItem> diseasePrevalence,
        LandUtilization landUtilization
) {}
