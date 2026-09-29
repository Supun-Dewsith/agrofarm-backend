package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.reports.*;
import com.agrofarm.api.repository.ReportRepository;
import com.agrofarm.api.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
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

        // Build dynamic financial trend for charts based on 'from' and 'to'
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);
        if (effectiveFrom.isAfter(effectiveTo)) {
            LocalDate tmp = effectiveFrom;
            effectiveFrom = effectiveTo;
            effectiveTo = tmp;
        }

        long daysBetween = ChronoUnit.DAYS.between(effectiveFrom, effectiveTo);
        List<Map<String, Object>> financialTrend = new ArrayList<>();

        if (daysBetween <= 31) {
            // Daily bucketing for 7d / 30d / short ranges
            Map<LocalDate, Map<String, BigDecimal>> dayBuckets = new LinkedHashMap<>();
            LocalDate curr = effectiveFrom;
            while (!curr.isAfter(effectiveTo)) {
                Map<String, BigDecimal> val = new HashMap<>();
                val.put("Revenue", BigDecimal.ZERO);
                val.put("Expense", BigDecimal.ZERO);
                dayBuckets.put(curr, val);
                curr = curr.plusDays(1);
            }

            for (FinancialTransactionDto f : financials) {
                if (f.getTransactionDate() != null && dayBuckets.containsKey(f.getTransactionDate())) {
                    Map<String, BigDecimal> val = dayBuckets.get(f.getTransactionDate());
                    if ("INCOME".equalsIgnoreCase(f.getRecordType())) {
                        val.put("Revenue", val.get("Revenue").add(f.getAmount()));
                    } else if ("EXPENSE".equalsIgnoreCase(f.getRecordType())) {
                        val.put("Expense", val.get("Expense").add(f.getAmount()));
                    }
                }
            }

            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH);
            dayBuckets.forEach((date, map) -> {
                Map<String, Object> point = new HashMap<>();
                point.put("month", date.format(dayFormatter));
                point.put("Revenue", map.get("Revenue"));
                point.put("Expense", map.get("Expense"));
                financialTrend.add(point);
            });
        } else {
            // Monthly bucketing for ranges > 31 days (e.g. 90d, 1y)
            YearMonth startYm = YearMonth.from(effectiveFrom);
            YearMonth endYm = YearMonth.from(effectiveTo);

            Map<YearMonth, Map<String, BigDecimal>> ymBuckets = new LinkedHashMap<>();
            YearMonth currYm = startYm;
            while (!currYm.isAfter(endYm)) {
                Map<String, BigDecimal> val = new HashMap<>();
                val.put("Revenue", BigDecimal.ZERO);
                val.put("Expense", BigDecimal.ZERO);
                ymBuckets.put(currYm, val);
                currYm = currYm.plusMonths(1);
            }

            for (FinancialTransactionDto f : financials) {
                if (f.getTransactionDate() != null) {
                    YearMonth ym = YearMonth.from(f.getTransactionDate());
                    if (ymBuckets.containsKey(ym)) {
                        Map<String, BigDecimal> val = ymBuckets.get(ym);
                        if ("INCOME".equalsIgnoreCase(f.getRecordType())) {
                            val.put("Revenue", val.get("Revenue").add(f.getAmount()));
                        } else if ("EXPENSE".equalsIgnoreCase(f.getRecordType())) {
                            val.put("Expense", val.get("Expense").add(f.getAmount()));
                        }
                    }
                }
            }

            boolean multiYear = startYm.getYear() != endYm.getYear();
            DateTimeFormatter ymFormatter = multiYear
                    ? DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH)
                    : DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

            ymBuckets.forEach((ym, map) -> {
                Map<String, Object> point = new HashMap<>();
                point.put("month", ym.format(ymFormatter));
                point.put("Revenue", map.get("Revenue"));
                point.put("Expense", map.get("Expense"));
                financialTrend.add(point);
            });
        }

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
