package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.dashboard.*;
import com.agrofarm.api.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public List<StatCardProp> getStatCardProps() {
        return List.of();
    }

    @Override
    public Weather getWeatherData() {
        return null;
    }

    @Override
    public List<CropHealth> getCropHealthData() {
        return List.of();
    }

    @Override
    public List<FinanceData> getFinanceData() {
        return List.of();
    }

    @Override
    public List<HarvestSummary> getHarvestSummary() {
        return List.of();
    }

    @Override
    public List<ActivityItem> getActivityItems() {
        return List.of();
    }
}
