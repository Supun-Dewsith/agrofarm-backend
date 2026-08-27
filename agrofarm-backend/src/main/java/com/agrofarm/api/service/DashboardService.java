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
}
