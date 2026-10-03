package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.dashboard.*;
import com.agrofarm.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<StatCardProp> getStatCardProps() {
        long farmsCount = 0;
        try {
            Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM farms", Long.class);
            if (c != null) farmsCount = c;
        } catch (Exception e) {
            log.warn("Failed to fetch farms count: {}", e.getMessage());
        }

        long activeCropsCount = 0;
        try {
            Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crops WHERE status != 'Harvested'", Long.class);
            if (c != null && c > 0) {
                activeCropsCount = c;
            } else {
                Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crops", Long.class);
                if (total != null) activeCropsCount = total;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch active crops count: {}", e.getMessage());
        }

        long activeTasksCount = 0;
        long pendingTasksCount = 0;
        try {
            Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks WHERE status IN ('PENDING', 'IN_PROGRESS')", Long.class);
            if (c != null) activeTasksCount = c;
            Long p = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tasks WHERE status = 'PENDING'", Long.class);
            if (p != null) pendingTasksCount = p;
        } catch (Exception e) {
            log.warn("Failed to fetch tasks count: {}", e.getMessage());
        }

        long diseaseAlertsCount = 0;
        try {
            Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM disease_detections WHERE is_healthy = 0", Long.class);
            if (c != null) diseaseAlertsCount = c;
        } catch (Exception e) {
            log.warn("Failed to fetch disease alerts: {}", e.getMessage());
        }

        double totalHarvestKg = 0.0;
        try {
            Double h = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(quantity_kg), 0) FROM harvest_records", Double.class);
            if (h != null) totalHarvestKg = h;
        } catch (Exception e) {
            log.warn("Failed to fetch harvest sum: {}", e.getMessage());
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        try {
            BigDecimal rev = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM farm_financial_records WHERE record_type = 'INCOME'",
                    BigDecimal.class
            );
            if (rev != null) totalRevenue = rev;
            BigDecimal exp = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM farm_financial_records WHERE record_type = 'EXPENSE'",
                    BigDecimal.class
            );
            if (exp != null) totalExpense = exp;
        } catch (Exception e) {
            log.warn("Failed to fetch finance sum: {}", e.getMessage());
        }

        long lowStockCount = 0;
        try {
            Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory WHERE quantity <= alertLimit", Long.class);
            if (c != null) lowStockCount = c;
        } catch (Exception e) {
            log.warn("Failed to fetch inventory alerts: {}", e.getMessage());
        }

        long farmersCount = 0;
        long supervisorsCount = 0;
        try {
            Long f = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE role = 'FARMER'", Long.class);
            if (f != null) farmersCount = f;
            Long s = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE role = 'SUPERVISOR'", Long.class);
            if (s != null) supervisorsCount = s;
        } catch (Exception e) {
            log.warn("Failed to fetch users count: {}", e.getMessage());
        }

        long onDutyTodayCount = 0;
        try {
            Long a = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM attendance WHERE date = CURRENT_DATE AND status IN ('PRESENT', 'LATE')",
                    Long.class
            );
            if (a != null) onDutyTodayCount = a;
        } catch (Exception e) {
            log.warn("Failed to fetch attendance count: {}", e.getMessage());
        }

        long totalWorkers = farmersCount + supervisorsCount;

        // Build distinct, non-duplicate stat cards covering all systems
        List<StatCardProp> cards = new ArrayList<>();

        cards.add(new StatCardProp(
                "Managed Farms",
                farmsCount,
                farmsCount > 0 ? farmsCount + " Active Farms" : "Setup Required",
                farmsCount > 0,
                "farms"
        ));

        cards.add(new StatCardProp(
                "Active Crops",
                activeCropsCount,
                activeCropsCount > 0 ? "Under Cultivation" : "None Planted",
                true,
                "crops"
        ));

        cards.add(new StatCardProp(
                "Active Tasks",
                activeTasksCount,
                pendingTasksCount > 0 ? pendingTasksCount + " Pending" : "Up to date",
                pendingTasksCount == 0,
                "tasks"
        ));

        cards.add(new StatCardProp(
                "Disease Alerts",
                diseaseAlertsCount,
                diseaseAlertsCount > 0 ? diseaseAlertsCount + " Under Review" : "All Crops Safe",
                diseaseAlertsCount == 0,
                "alerts"
        ));

        cards.add(new StatCardProp(
                "Total Harvest",
                totalHarvestKg > 0 ? String.format(Locale.US, "%,.1f kg", totalHarvestKg) : "0 kg",
                "Yield Logged",
                true,
                "harvest"
        ));

        cards.add(new StatCardProp(
                "Total Revenue",
                totalRevenue.compareTo(BigDecimal.ZERO) > 0
                        ? String.format(Locale.US, "LKR %,.0f", totalRevenue.doubleValue())
                        : "LKR 0",
                totalRevenue.compareTo(totalExpense) >= 0 ? "Healthy Margin" : "Net Deficit",
                totalRevenue.compareTo(totalExpense) >= 0,
                "revenue"
        ));

        cards.add(new StatCardProp(
                "Low Stock Items",
                lowStockCount,
                lowStockCount > 0 ? lowStockCount + " Items Restock" : "Inventory Stable",
                lowStockCount == 0,
                "inventory"
        ));

        cards.add(new StatCardProp(
                "Field Team",
                totalWorkers,
                onDutyTodayCount > 0 ? onDutyTodayCount + " On Duty Today" : (farmersCount > 0 ? farmersCount + " Farmers Active" : "No Staff Logged"),
                totalWorkers > 0,
                "workers"
        ));

        return cards;
    }

    @Override
    public Weather getWeatherData() {
        try {
            List<Weather> list = jdbcTemplate.query(
                    "SELECT temperature, humidity, rain, wind_speed, weather_code, weather_date " +
                    "FROM weather_history ORDER BY weather_date DESC, id DESC LIMIT 1",
                    (rs, rowNum) -> new Weather(
                            (int) Math.round(rs.getDouble("temperature")),
                            resolveWeatherText(rs.getInt("weather_code")),
                            rs.getDate("weather_date") != null ? rs.getDate("weather_date").toString() : LocalDate.now().toString(),
                            rs.getInt("humidity") + "%",
                            rs.getDouble("rain") + "mm",
                            rs.getDouble("wind_speed") + " km/h"
                    )
            );
            if (!list.isEmpty()) {
                return list.get(0);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch weather from database: {}", e.getMessage());
        }
        return new Weather(
                0,
                "No Weather Recorded",
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                "0%",
                "0mm",
                "0 km/h"
        );
    }

    @Override
    public List<CropHealth> getCropHealthData() {
        long healthyCount = 0;
        long moderateCount = 0;
        long criticalCount = 0;

        try {
            List<Map<String, Object>> cropStatuses = jdbcTemplate.queryForList(
                    "SELECT status, COUNT(*) as cnt FROM crops GROUP BY status"
            );
            for (Map<String, Object> row : cropStatuses) {
                String status = row.get("status") != null ? row.get("status").toString().toLowerCase() : "";
                long cnt = ((Number) row.get("cnt")).longValue();
                if (status.contains("healthy") || status.contains("active") || status.contains("normal")) {
                    healthyCount += cnt;
                } else if (status.contains("moderate") || status.contains("flowering") || status.contains("vegetative")) {
                    moderateCount += cnt;
                } else if (status.contains("critical") || status.contains("diseased") || status.contains("danger")) {
                    criticalCount += cnt;
                } else {
                    healthyCount += cnt;
                }
            }
        } catch (Exception ignored) {}

        try {
            List<Map<String, Object>> detections = jdbcTemplate.queryForList(
                    "SELECT is_healthy, COUNT(*) as cnt FROM disease_detections GROUP BY is_healthy"
            );
            for (Map<String, Object> row : detections) {
                boolean healthy = Objects.equals(row.get("is_healthy"), 1) || Boolean.TRUE.equals(row.get("is_healthy"));
                long cnt = ((Number) row.get("cnt")).longValue();
                if (healthy) {
                    healthyCount += cnt;
                } else {
                    criticalCount += cnt;
                }
            }
        } catch (Exception ignored) {}

        long total = healthyCount + moderateCount + criticalCount;
        if (total == 0) {
            return List.of();
        }

        int healthyPct = (int) Math.round((healthyCount * 100.0) / total);
        int criticalPct = (int) Math.round((criticalCount * 100.0) / total);
        int moderatePct = Math.max(0, 100 - healthyPct - criticalPct);

        return List.of(
                new CropHealth("Healthy", healthyPct, "#10b981"),
                new CropHealth("Moderate", moderatePct, "#eab308"),
                new CropHealth("Critical", criticalPct, "#ef4444")
        );
    }

    @Override
    public List<FinanceData> getFinanceData() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter keyFmt = DateTimeFormatter.ofPattern("MMM", Locale.US);

        Map<String, FinanceData> monthMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = today.minusMonths(i);
            String monthName = d.format(keyFmt);
            monthMap.put(monthName, new FinanceData(monthName, 0.0, 0.0));
        }

        try {
            String sql = """
                    SELECT
                        DATE_FORMAT(transaction_date, '%b') as m_name,
                        SUM(CASE WHEN record_type = 'INCOME' THEN amount ELSE 0 END) as rev,
                        SUM(CASE WHEN record_type = 'EXPENSE' THEN amount ELSE 0 END) as exp
                    FROM farm_financial_records
                    WHERE transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH)
                    GROUP BY DATE_FORMAT(transaction_date, '%b'), YEAR(transaction_date), MONTH(transaction_date)
                    ORDER BY YEAR(transaction_date) ASC, MONTH(transaction_date) ASC
                    """;

            jdbcTemplate.query(sql, rs -> {
                String m = rs.getString("m_name");
                double rev = rs.getDouble("rev");
                double exp = rs.getDouble("exp");
                if (monthMap.containsKey(m)) {
                    monthMap.put(m, new FinanceData(m, rev, exp));
                }
            });
        } catch (Exception e) {
            log.warn("Failed to fetch finance history: {}", e.getMessage());
        }

        return new ArrayList<>(monthMap.values());
    }

    @Override
    public List<HarvestSummary> getHarvestSummary() {
        List<HarvestSummary> results = new ArrayList<>();

        try {
            String sql = """
                    SELECT c.crop_name, COALESCE(SUM(h.quantity_kg), 0) as actual_qty
                    FROM harvest_records h
                    JOIN crops c ON c.crop_id = h.crop_id
                    GROUP BY c.crop_name
                    ORDER BY actual_qty DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                String cropName = rs.getString("crop_name");
                int actual = (int) Math.round(rs.getDouble("actual_qty"));
                int target = (int) Math.round(actual * 1.2);
                results.add(new HarvestSummary(cropName, actual, target));
            });
        } catch (Exception e) {
            log.warn("Failed to fetch harvest summary: {}", e.getMessage());
        }

        return results;
    }

    @Override
    public List<ActivityItem> getActivityItems() {
        record ScoredActivity(ActivityItem item, LocalDateTime time) {}
        List<ScoredActivity> list = new ArrayList<>();

        // 1. Disease detections
        try {
            String sql = """
                    SELECT d.id, d.plant_name, d.disease_name, d.is_healthy, d.created_at, u.user_name
                    FROM disease_detections d
                    LEFT JOIN users u ON d.user_id = u.id
                    ORDER BY d.created_at DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long id = rs.getLong("id");
                String plant = rs.getString("plant_name");
                String disease = rs.getString("disease_name");
                boolean healthy = rs.getBoolean("is_healthy");
                Timestamp ts = rs.getTimestamp("created_at");
                String user = rs.getString("user_name");
                LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                String title = healthy ? "Healthy Crop Scanned (" + plant + ")" : "Disease Alert: " + disease;
                String desc = plant + " • Scanned by " + (user != null ? user : "Scout");
                list.add(new ScoredActivity(
                        new ActivityItem("disease-" + id, "disease", title, desc, formatRelativeTime(ldt)),
                        ldt
                ));
            });
        } catch (Exception ignored) {}

        // 2. Tasks
        try {
            String sql = """
                    SELECT t.id, t.title, t.priority, t.status, t.created_at, u.user_name
                    FROM tasks t
                    LEFT JOIN users u ON t.assignee_id = u.id
                    ORDER BY t.created_at DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                String prio = rs.getString("priority");
                String status = rs.getString("status");
                Timestamp ts = rs.getTimestamp("created_at");
                String user = rs.getString("user_name");
                LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                String desc = "Priority: " + prio + " • Status: " + status + (user != null ? " • " + user : "");
                list.add(new ScoredActivity(
                        new ActivityItem("task-" + id, "task", "Task: " + title, desc, formatRelativeTime(ldt)),
                        ldt
                ));
            });
        } catch (Exception ignored) {}

        // 3. Harvest records
        try {
            String sql = """
                    SELECT h.id, c.crop_name, h.quantity_kg, h.harvested_on, h.created_at, u.user_name
                    FROM harvest_records h
                    JOIN crops c ON c.crop_id = h.crop_id
                    LEFT JOIN users u ON h.created_by = u.id
                    ORDER BY h.created_at DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long id = rs.getLong("id");
                String crop = rs.getString("crop_name");
                double qty = rs.getDouble("quantity_kg");
                Timestamp ts = rs.getTimestamp("created_at");
                String user = rs.getString("user_name");
                LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                String title = "Harvest Logged: " + String.format(Locale.US, "%,.1f kg", qty) + " " + crop;
                String desc = "Recorded by " + (user != null ? user : "Farm Staff");
                list.add(new ScoredActivity(
                        new ActivityItem("harvest-" + id, "harvest", title, desc, formatRelativeTime(ldt)),
                        ldt
                ));
            });
        } catch (Exception ignored) {}

        // 4. Financial records
        try {
            String sql = """
                    SELECT r.id, r.category, r.record_type, r.amount, r.created_at, f.farm_name
                    FROM farm_financial_records r
                    LEFT JOIN farms f ON r.farm_id = f.id
                    ORDER BY r.created_at DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long id = rs.getLong("id");
                String cat = rs.getString("category");
                String type = rs.getString("record_type");
                double amt = rs.getDouble("amount");
                Timestamp ts = rs.getTimestamp("created_at");
                String farm = rs.getString("farm_name");
                LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                String prefix = "INCOME".equalsIgnoreCase(type) ? "+ LKR " : "- LKR ";
                String title = type + ": " + cat + " (" + prefix + String.format(Locale.US, "%,.0f", amt) + ")";
                String desc = farm != null ? farm : "Farm Estate";
                list.add(new ScoredActivity(
                        new ActivityItem("fin-" + id, "finance", title, desc, formatRelativeTime(ldt)),
                        ldt
                ));
            });
        } catch (Exception ignored) {}

        // 5. Attendance records
        try {
            String sql = """
                    SELECT a.id, a.user_name, a.status, a.date, a.check_in_time
                    FROM attendance a
                    ORDER BY a.date DESC, a.check_in_time DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long id = rs.getLong("id");
                String name = rs.getString("user_name");
                String status = rs.getString("status");
                Timestamp ts = rs.getTimestamp("check_in_time");
                LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                String title = "Attendance Check-In: " + name;
                String desc = "Status: " + status;
                list.add(new ScoredActivity(
                        new ActivityItem("att-" + id, "attendance", title, desc, formatRelativeTime(ldt)),
                        ldt
                ));
            });
        } catch (Exception ignored) {}

        // Sort descending by time
        list.sort((a, b) -> b.time().compareTo(a.time()));

        List<ActivityItem> result = new ArrayList<>();
        int max = Math.min(list.size(), 10);
        for (int i = 0; i < max; i++) {
            result.add(list.get(i).item());
        }

        return result;
    }

    @Override
    public TaskDistribution getTaskDistribution() {
        try {
            String sql = """
                    SELECT
                        COUNT(*) as total,
                        COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
                        COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress,
                        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
                        COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as high_prio,
                        COUNT(CASE WHEN priority = 'MEDIUM' THEN 1 END) as med_prio,
                        COUNT(CASE WHEN priority = 'LOW' THEN 1 END) as low_prio
                    FROM tasks
                    """;
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                long total = rs.getLong("total");
                long pending = rs.getLong("pending");
                long inProg = rs.getLong("in_progress");
                long comp = rs.getLong("completed");
                double rate = total > 0 ? Math.round((comp * 100.0) / total) : 0.0;
                return new TaskDistribution(
                        total, pending, inProg, comp,
                        rs.getLong("high_prio"),
                        rs.getLong("med_prio"),
                        rs.getLong("low_prio"),
                        rate
                );
            });
        } catch (Exception e) {
            log.warn("Failed to fetch task distribution: {}", e.getMessage());
            return new TaskDistribution(0, 0, 0, 0, 0, 0, 0, 0.0);
        }
    }

    @Override
    public List<InventoryAlertItem> getLowStockAlerts() {
        try {
            String sql = """
                    SELECT i.id, i.inventory_name, i.category, i.quantity, i.unit, i.alertLimit, f.farm_name
                    FROM inventory i
                    LEFT JOIN farms f ON i.farm_id = f.id
                    WHERE i.quantity <= i.alertLimit
                    ORDER BY i.quantity ASC
                    LIMIT 10
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryAlertItem(
                    rs.getLong("id"),
                    rs.getString("inventory_name"),
                    rs.getString("category"),
                    rs.getDouble("quantity"),
                    rs.getString("unit"),
                    rs.getDouble("alertLimit"),
                    rs.getString("farm_name") != null ? rs.getString("farm_name") : "Central Store",
                    "LOW_STOCK"
            ));
        } catch (Exception e) {
            log.warn("Failed to fetch low stock alerts: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<FarmOverviewItem> getFarmOverviews() {
        try {
            String sql = """
                    SELECT
                        f.id,
                        f.farm_name,
                        f.farm_address as location,
                        (SELECT COUNT(*) FROM fields WHERE farm_id = f.id) as fields_count,
                        (SELECT COUNT(*) FROM crops WHERE farm_id = f.id) as crops_count,
                        (SELECT COUNT(*) FROM tasks t JOIN fields fi ON t.field_id = fi.id WHERE fi.farm_id = f.id AND t.status != 'COMPLETED') as active_tasks_count
                    FROM farms f
                    ORDER BY f.id ASC
                    LIMIT 8
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> new FarmOverviewItem(
                    rs.getLong("id"),
                    rs.getString("farm_name"),
                    rs.getString("location") != null ? rs.getString("location") : "Coordinates Set",
                    rs.getInt("fields_count"),
                    rs.getInt("crops_count"),
                    rs.getInt("active_tasks_count")
            ));
        } catch (Exception e) {
            log.warn("Failed to fetch farm overviews: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<CropStageDistribution> getCropStageDistribution() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Germination", 0);
        counts.put("Vegetative", 0);
        counts.put("Flowering", 0);
        counts.put("Ripening", 0);
        counts.put("Harvest Ready", 0);

        try {
            String sql = "SELECT growth_stage, COUNT(*) as cnt FROM crops WHERE growth_stage IS NOT NULL GROUP BY growth_stage";
            jdbcTemplate.query(sql, rs -> {
                String stage = rs.getString("growth_stage");
                int cnt = rs.getInt("cnt");
                if (stage != null) {
                    String lower = stage.toLowerCase();
                    if (lower.contains("germin") || lower.contains("seed")) counts.put("Germination", counts.get("Germination") + cnt);
                    else if (lower.contains("veget")) counts.put("Vegetative", counts.get("Vegetative") + cnt);
                    else if (lower.contains("flower") || lower.contains("bloom")) counts.put("Flowering", counts.get("Flowering") + cnt);
                    else if (lower.contains("ripen")) counts.put("Ripening", counts.get("Ripening") + cnt);
                    else if (lower.contains("harvest") || lower.contains("mature")) counts.put("Harvest Ready", counts.get("Harvest Ready") + cnt);
                    else counts.put("Vegetative", counts.get("Vegetative") + cnt);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to fetch crop stages: {}", e.getMessage());
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            return List.of();
        }

        Map<String, String> colors = Map.of(
                "Germination", "#38bdf8",
                "Vegetative", "#10b981",
                "Flowering", "#f59e0b",
                "Ripening", "#f97316",
                "Harvest Ready", "#84cc16"
        );

        List<CropStageDistribution> list = new ArrayList<>();
        counts.forEach((stage, cnt) -> {
            int pct = (int) Math.round((cnt * 100.0) / total);
            list.add(new CropStageDistribution(stage, cnt, pct, colors.getOrDefault(stage, "#10b981")));
        });
        return list;
    }

    @Override
    public List<UpcomingHarvestItem> getUpcomingHarvests() {
        List<UpcomingHarvestItem> list = new ArrayList<>();
        try {
            String sql = """
                    SELECT c.crop_id, c.crop_name, c.variety, f.farm_name, c.progress, c.harvest_date
                    FROM crops c
                    LEFT JOIN farms f ON c.farm_id = f.id
                    WHERE c.status IS NULL OR c.status != 'Harvested'
                    ORDER BY c.harvest_date ASC, c.progress DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                long cropId = rs.getLong("crop_id");
                String cropName = rs.getString("crop_name");
                String variety = rs.getString("variety");
                String farmName = rs.getString("farm_name");
                int progress = rs.getInt("progress");
                Timestamp hDate = rs.getTimestamp("harvest_date");

                long daysRem = 14;
                String harvestStr = "Next 2 Weeks";
                if (hDate != null) {
                    LocalDate d = hDate.toLocalDateTime().toLocalDate();
                    harvestStr = d.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
                    daysRem = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d));
                } else if (progress > 0) {
                    daysRem = Math.max(1, (100 - progress) / 3);
                    harvestStr = "In approx " + daysRem + " days";
                }

                list.add(new UpcomingHarvestItem(
                        cropId,
                        cropName != null ? cropName : "Crop #" + cropId,
                        variety != null ? variety : "Standard",
                        farmName != null ? farmName : "Main Estate",
                        progress,
                        harvestStr,
                        daysRem
                ));
            });
        } catch (Exception e) {
            log.warn("Failed to fetch upcoming harvests: {}", e.getMessage());
        }

        return list;
    }

    @Override
    public List<ExpenseCategoryBreakdown> getExpenseCategoryBreakdown() {
        List<ExpenseCategoryBreakdown> list = new ArrayList<>();
        double totalExpense = 0.0;
        try {
            String sql = """
                    SELECT category, SUM(amount) as total
                    FROM farm_financial_records
                    WHERE record_type = 'EXPENSE'
                    GROUP BY category
                    ORDER BY total DESC
                    LIMIT 6
                    """;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> r : rows) {
                totalExpense += ((Number) r.get("total")).doubleValue();
            }

            String[] colors = new String[]{"#059669", "#0284c7", "#d97706", "#7c3aed", "#e11d48", "#475569"};
            int colorIdx = 0;
            for (Map<String, Object> r : rows) {
                String cat = (String) r.get("category");
                double amt = ((Number) r.get("total")).doubleValue();
                int pct = totalExpense > 0 ? (int) Math.round((amt * 100.0) / totalExpense) : 0;
                list.add(new ExpenseCategoryBreakdown(cat, amt, pct, colors[colorIdx % colors.length]));
                colorIdx++;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch expense breakdown: {}", e.getMessage());
        }

        return list;
    }

    @Override
    public List<AttendanceTrendDay> getAttendanceTrend() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("EEE", Locale.US);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd", Locale.US);

        Map<LocalDate, AttendanceTrendDay> dayMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            dayMap.put(d, new AttendanceTrendDay(d.format(dayFmt), d.format(dateFmt), 0, 0, 0));
        }

        try {
            String sql = """
                    SELECT
                        date,
                        COUNT(CASE WHEN status = 'PRESENT' THEN 1 END) as present_cnt,
                        COUNT(CASE WHEN status = 'LATE' THEN 1 END) as late_cnt,
                        COUNT(CASE WHEN status = 'ABSENT' THEN 1 END) as absent_cnt
                    FROM attendance
                    WHERE date >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
                    GROUP BY date
                    ORDER BY date ASC
                    """;

            jdbcTemplate.query(sql, rs -> {
                java.sql.Date sqlDate = rs.getDate("date");
                if (sqlDate != null) {
                    LocalDate d = sqlDate.toLocalDate();
                    if (dayMap.containsKey(d)) {
                        dayMap.put(d, new AttendanceTrendDay(
                                d.format(dayFmt),
                                d.format(dateFmt),
                                rs.getInt("present_cnt"),
                                rs.getInt("late_cnt"),
                                rs.getInt("absent_cnt")
                        ));
                    }
                }
            });
        } catch (Exception e) {
            log.warn("Failed to fetch attendance trend: {}", e.getMessage());
        }

        return new ArrayList<>(dayMap.values());
    }

    @Override
    public List<DiseasePrevalenceItem> getDiseasePrevalence() {
        List<DiseasePrevalenceItem> list = new ArrayList<>();
        try {
            String sql = """
                    SELECT disease_name, plant_name, COUNT(*) as cnt, COALESCE(AVG(overall_confidence), 0.9) as avg_conf
                    FROM disease_detections
                    WHERE is_healthy = 0
                    GROUP BY disease_name, plant_name
                    ORDER BY cnt DESC
                    LIMIT 5
                    """;
            jdbcTemplate.query(sql, rs -> {
                double conf = rs.getDouble("avg_conf");
                if (conf > 0 && conf <= 1.0) conf = conf * 100.0;
                list.add(new DiseasePrevalenceItem(
                        rs.getString("disease_name"),
                        rs.getString("plant_name"),
                        rs.getInt("cnt"),
                        Math.round(conf * 10.0) / 10.0
                ));
            });
        } catch (Exception e) {
            log.warn("Failed to fetch disease prevalence: {}", e.getMessage());
        }

        return list;
    }

    @Override
    public LandUtilization getLandUtilization() {
        double totalArea = 0.0;
        int totalFields = 0;
        Map<String, Integer> distribution = new LinkedHashMap<>();

        try {
            Double a = jdbcTemplate.queryForObject("""
                    SELECT COALESCE(SUM(
                        CASE 
                            WHEN size_unit = 'HECTARE' THEN field_size * 2.47105
                            WHEN size_unit = 'SQ_METER' THEN field_size * 0.000247105
                            ELSE field_size
                        END
                    ), 0) FROM fields
                    """, Double.class);
            if (a != null) totalArea = Math.round(a * 10.0) / 10.0;
            Integer f = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fields", Integer.class);
            if (f != null) totalFields = f;

            String sql = """
                    SELECT f.farm_name, COUNT(*) as cnt
                    FROM fields fi
                    JOIN farms f ON fi.farm_id = f.id
                    GROUP BY f.farm_name
                    ORDER BY cnt DESC
                    """;
            jdbcTemplate.query(sql, rs -> {
                distribution.put(rs.getString("farm_name"), rs.getInt("cnt"));
            });
        } catch (Exception e) {
            log.warn("Failed to fetch land utilization: {}", e.getMessage());
        }

        return new LandUtilization(totalArea, totalFields, distribution);
    }

    @Override
    public DashboardOverviewResponse getDashboardOverview() {
        return new DashboardOverviewResponse(
                getStatCardProps(),
                getWeatherData(),
                getCropHealthData(),
                getFinanceData(),
                getHarvestSummary(),
                getActivityItems(),
                getTaskDistribution(),
                getLowStockAlerts(),
                getFarmOverviews(),
                getCropStageDistribution(),
                getUpcomingHarvests(),
                getExpenseCategoryBreakdown(),
                getAttendanceTrend(),
                getDiseasePrevalence(),
                getLandUtilization()
        );
    }

    private String resolveWeatherText(int code) {
        return switch (code) {
            case 0 -> "Clear Sky";
            case 1 -> "Mainly Clear";
            case 2 -> "Partly Cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Light Drizzle";
            case 61, 63, 65 -> "Rain";
            case 80, 81, 82 -> "Rain Showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Partly Cloudy";
        };
    }

    private String formatRelativeTime(LocalDateTime ldt) {
        if (ldt == null) return "Recent";
        Duration diff = Duration.between(ldt, LocalDateTime.now());
        long mins = diff.toMinutes();
        if (mins < 1) return "Just now";
        if (mins < 60) return mins + "m ago";
        long hours = diff.toHours();
        if (hours < 24) return hours + "h ago";
        long days = diff.toDays();
        if (days == 1) return "Yesterday";
        if (days < 30) return days + "d ago";
        return ldt.format(DateTimeFormatter.ofPattern("MMM d"));
    }
}