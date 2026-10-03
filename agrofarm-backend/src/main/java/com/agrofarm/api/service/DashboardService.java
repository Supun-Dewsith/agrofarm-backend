package com.agrofarm.api.service;

import com.agrofarm.api.dto.dashboard.*;

import java.util.List;

public interface DashboardService {
    List<StatCardProp> getStatCardProps();
    Weather getWeatherData();
    List<CropHealth> getCropHealthData();
    List<FinanceData> getFinanceData();
    List<HarvestSummary> getHarvestSummary();
    List<ActivityItem> getActivityItems();
    TaskDistribution getTaskDistribution();
    List<InventoryAlertItem> getLowStockAlerts();
    List<FarmOverviewItem> getFarmOverviews();
    List<CropStageDistribution> getCropStageDistribution();
    List<UpcomingHarvestItem> getUpcomingHarvests();
    List<ExpenseCategoryBreakdown> getExpenseCategoryBreakdown();
    List<AttendanceTrendDay> getAttendanceTrend();
    List<DiseasePrevalenceItem> getDiseasePrevalence();
    LandUtilization getLandUtilization();
    DashboardOverviewResponse getDashboardOverview();
}
