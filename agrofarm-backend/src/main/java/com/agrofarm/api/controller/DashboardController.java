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
}
