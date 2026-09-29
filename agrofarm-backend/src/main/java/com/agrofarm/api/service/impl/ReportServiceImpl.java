package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.reports.*;
import com.agrofarm.api.repository.ReportRepository;
import com.agrofarm.api.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public ReportSummaryDto getSummary(Long farmId, LocalDate from, LocalDate to) {
        List<FinancialTransactionDto> financials = reportRepository.findFinancialTransactions(farmId, from, to);
        List<CropReportItemDto> crops = reportRepository.findCropReports(farmId);
        List<TaskReportItemDto> tasks = reportRepository.findTaskReports(farmId, from, to);
        List<AttendanceReportItemDto> attendance = reportRepository.findAttendanceReports(farmId, from, to);
        List<DiseaseReportItemDto> disease = reportRepository.findDiseaseReports(50);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (FinancialTransactionDto f : financials) {
            if ("INCOME".equalsIgnoreCase(f.getRecordType())) {
                totalIncome = totalIncome.add(f.getAmount());
            } else if ("EXPENSE".equalsIgnoreCase(f.getRecordType())) {
                totalExpense = totalExpense.add(f.getAmount());
            }
        }

        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        double profitMargin = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            profitMargin = netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        long healthyCrops = crops.stream().filter(c -> "Healthy".equalsIgnoreCase(c.getStatus())).count();
        long completedTasks = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long pendingTasks = tasks.stream().filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long diseaseAlerts = disease.stream().filter(d -> Boolean.FALSE.equals(d.getIsHealthy())).count();

        // Build monthly financial trend for charts
        Map<String, Map<String, BigDecimal>> monthBuckets = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            String label = m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            Map<String, BigDecimal> val = new HashMap<>();
            val.put("Revenue", BigDecimal.ZERO);
            val.put("Expense", BigDecimal.ZERO);
            monthBuckets.put(label, val);
        }

        for (FinancialTransactionDto f : financials) {
            String label = f.getTransactionDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            if (monthBuckets.containsKey(label)) {
                Map<String, BigDecimal> val = monthBuckets.get(label);
                if ("INCOME".equalsIgnoreCase(f.getRecordType())) {
                    val.put("Revenue", val.get("Revenue").add(f.getAmount()));
                } else {
                    val.put("Expense", val.get("Expense").add(f.getAmount()));
                }
            }
        }

        List<Map<String, Object>> financialTrend = new ArrayList<>();
        monthBuckets.forEach((month, map) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("month", month);
            point.put("Revenue", map.get("Revenue"));
            point.put("Expense", map.get("Expense"));
            financialTrend.add(point);
        });

        // Crop stage distribution
        Map<String, Integer> stageCounts = new HashMap<>();
        for (CropReportItemDto c : crops) {
            String stage = c.getGrowthStage() != null ? c.getGrowthStage() : "Other";
            stageCounts.put(stage, stageCounts.getOrDefault(stage, 0) + 1);
        }
        List<Map<String, Object>> cropDistribution = new ArrayList<>();
        stageCounts.forEach((stage, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("stage", stage);
            item.put("count", count);
            cropDistribution.add(item);
        });

        // Task priority distribution
        Map<String, Integer> priorityCounts = new HashMap<>();
        for (TaskReportItemDto t : tasks) {
            String p = t.getPriority() != null ? t.getPriority() : "MEDIUM";
            priorityCounts.put(p, priorityCounts.getOrDefault(p, 0) + 1);
        }
        List<Map<String, Object>> taskDistribution = new ArrayList<>();
        priorityCounts.forEach((p, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("priority", p);
            item.put("count", count);
            taskDistribution.add(item);
        });

        return ReportSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .activeCropsCount((long) crops.size())
                .healthyCropsCount(healthyCrops)
                .completedTasksCount(completedTasks)
                .pendingTasksCount(pendingTasks)
                .totalAttendanceCount((long) attendance.size())
                .diseaseAlertsCount(diseaseAlerts)
                .financialTrend(financialTrend)
                .cropStatusDistribution(cropDistribution)
                .taskPriorityDistribution(taskDistribution)
                .build();
    }

    @Override
    public List<FinancialTransactionDto> getFinancialReport(Long farmId, LocalDate from, LocalDate to) {
        return reportRepository.findFinancialTransactions(farmId, from, to);
    }

    @Override
    public List<CropReportItemDto> getCropReport(Long farmId) {
        return reportRepository.findCropReports(farmId);
    }

    @Override
    public List<AttendanceReportItemDto> getAttendanceReport(Long farmId, LocalDate from, LocalDate to) {
        return reportRepository.findAttendanceReports(farmId, from, to);
    }

    @Override
    public List<TaskReportItemDto> getTaskReport(Long farmId, LocalDate from, LocalDate to) {
        return reportRepository.findTaskReports(farmId, from, to);
    }

    @Override
    public List<DiseaseReportItemDto> getDiseaseReport(int limit) {
        return reportRepository.findDiseaseReports(limit);
    }

    @Override
    public FinancialTransactionDto createFinancialTransaction(CreateFinancialTransactionRequest request, Long userId) {
        return reportRepository.insertFinancialTransaction(request, userId);
    }
}
