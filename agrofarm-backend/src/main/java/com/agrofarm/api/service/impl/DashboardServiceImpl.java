package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.dashboard.*;
import com.agrofarm.api.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public List<StatCardProp> getStatCardProps() {
        return List.of(
                new StatCardProp("Total Farmers", 156, "+12%", true, "farmers"),
                new StatCardProp("Total Workers", 342, "+8%", true, "workers"),
                new StatCardProp("Active Crops", 24, "+3", true, "crops"),
                new StatCardProp("Active Tasks", 67, "-6", false, "tasks"),
                new StatCardProp("Disease Alerts", 8, "+1", false, "alerts"),
                new StatCardProp("Total Revenue", "$405.2K", "+18%", true, "revenue"),
                new StatCardProp("Total Farmers", 156, "+12%", true, "farmers"),
                new StatCardProp("Total Workers", 342, "+8%", true, "workers"),
                new StatCardProp("Active Crops", 24, "+3", true, "crops")

        );
    }

    @Override
    public Weather getWeatherData() {
        return new Weather(
                28,
                "Partly Cloudy",
                "Today, June 21",
                "65%",
                "12mm",
                "8 km/h"
        );
    }

    @Override
    public List<CropHealth> getCropHealthData() {
        return List.of(
                new CropHealth("Healthy", 68, "#10b981"),
                new CropHealth("Moderate", 22, "#eab308"),
                new CropHealth("Critical", 10, "#ef4444")
        );
    }

    @Override
    public List<FinanceData> getFinanceData() {
        return List.of(
                new FinanceData("Jan", 28000.0, 18000.0),
                new FinanceData("Feb", 32000.0, 20000.0),
                new FinanceData("Mar", 35000.0, 21000.0),
                new FinanceData("Apr", 39000.0, 22000.0),
                new FinanceData("May", 43000.0, 24000.0),
                new FinanceData("Jun", 45000.0, 25000.0)
        );
    }

    @Override
    public List<HarvestSummary> getHarvestSummary() {
        return List.of(
                new HarvestSummary("Tomato", 480, 550),
                new HarvestSummary("Chili", 510, 600),
                new HarvestSummary("Rice", 490, 530),
                new HarvestSummary("Corn", 520, 410),
                new HarvestSummary("Potato", 540, 560)
        );
    }

    @Override
    public List<ActivityItem> getActivityItems() {
        return List.of(
                new ActivityItem("1", "disease", "New disease detected", "Tomato crop in Field A", "5 minutes ago"),
                new ActivityItem("2", "task", "Task assigned", "Irrigation for Field B", "2 hours ago")
        );
    }
}