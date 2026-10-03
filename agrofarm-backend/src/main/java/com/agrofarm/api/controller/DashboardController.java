package com.agrofarm.api.controller;

import com.agrofarm.api.dto.dashboard.*;
import com.agrofarm.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<List<StatCardProp>> getStatCardProps() {
        return ResponseEntity.ok(dashboardService.getStatCardProps());
    }

    @GetMapping("/weather")
    public ResponseEntity<Weather> getWeatherData() {
        return ResponseEntity.ok(dashboardService.getWeatherData());
    }

    @GetMapping("/health")
    public ResponseEntity<List<CropHealth>> getHealthMetrics() {
        return ResponseEntity.ok(dashboardService.getCropHealthData());
    }

    @GetMapping("/finance")
    public ResponseEntity<List<FinanceData>> getFinanceData() {
        return ResponseEntity.ok(dashboardService.getFinanceData());
    }

    @GetMapping("/harvest")
    public ResponseEntity<List<HarvestSummary>> getHarvestSummary() {
        return ResponseEntity.ok(dashboardService.getHarvestSummary());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityItem>> getActivityItems() {
        return ResponseEntity.ok(dashboardService.getActivityItems());
    }

    @GetMapping("/tasks")
    public ResponseEntity<TaskDistribution> getTaskDistribution() {
        return ResponseEntity.ok(dashboardService.getTaskDistribution());
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryAlertItem>> getLowStockAlerts() {
        return ResponseEntity.ok(dashboardService.getLowStockAlerts());
    }

    @GetMapping("/farms-summary")
    public ResponseEntity<List<FarmOverviewItem>> getFarmOverviews() {
        return ResponseEntity.ok(dashboardService.getFarmOverviews());
    }

    @GetMapping("/crop-stages")
    public ResponseEntity<List<CropStageDistribution>> getCropStageDistribution() {
        return ResponseEntity.ok(dashboardService.getCropStageDistribution());
    }

    @GetMapping("/upcoming-harvests")
    public ResponseEntity<List<UpcomingHarvestItem>> getUpcomingHarvests() {
        return ResponseEntity.ok(dashboardService.getUpcomingHarvests());
    }

    @GetMapping("/expenses-breakdown")
    public ResponseEntity<List<ExpenseCategoryBreakdown>> getExpenseCategoryBreakdown() {
        return ResponseEntity.ok(dashboardService.getExpenseCategoryBreakdown());
    }

    @GetMapping("/attendance-trend")
    public ResponseEntity<List<AttendanceTrendDay>> getAttendanceTrend() {
        return ResponseEntity.ok(dashboardService.getAttendanceTrend());
    }

    @GetMapping("/disease-prevalence")
    public ResponseEntity<List<DiseasePrevalenceItem>> getDiseasePrevalence() {
        return ResponseEntity.ok(dashboardService.getDiseasePrevalence());
    }

    @GetMapping("/land-utilization")
    public ResponseEntity<LandUtilization> getLandUtilization() {
        return ResponseEntity.ok(dashboardService.getLandUtilization());
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        return ResponseEntity.ok(dashboardService.getDashboardOverview());
    }
}
