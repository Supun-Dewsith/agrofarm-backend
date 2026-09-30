package com.agrofarm.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class OfficialPriceService {
    private static final Logger log = LoggerFactory.getLogger(OfficialPriceService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Colombo");
    private static final String INDEX =
            "https://www.cbsl.gov.lk/en/statistics/economic-indicators/price-report";
    private static final long CHECK_INTERVAL_MS = 3_600_000L;
    private static final long RETRY_MS = 300_000L;
    // Longer than the bounded three-report download attempt.
    private static final long LEASE_MS = 600_000L;
    private final JdbcTemplate jdbc;
    private final OfficialPriceSource source;
    private final TransactionTemplate tx;

    public OfficialPriceService(JdbcTemplate jdbc, OfficialPriceSource source,
                                PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.source = source;
        this.tx = new TransactionTemplate(manager);
        this.tx.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }

    public record SavedPrice(String commodity, String market, BigDecimal priceLkrPerKg,
                             LocalDate observedOn, String sourceUrl) {}
    public record Result(String status, LocalDate reportDate, Instant fetchedAt,
                         Instant lastCheckedAt, Instant lastSuccessfulCheckAt,
                         String sourceUrl, String warning, List<SavedPrice> prices) {}

    /** Short DB lease; external HTTP requests never hold a database transaction. */
    @Scheduled(initialDelay = 15_000L, fixedDelay = 60_000L)
    public synchronized void refresh() {
        long now = System.currentTimeMillis();
        long lease = now + LEASE_MS;
        try {
            int claimed = jdbc.update("""
                UPDATE official_price_sync SET next_check_ms=?
                WHERE id=1 AND next_check_ms<=?
                """, lease, now);
            if (claimed == 0) return;
            try {
                var report = source.download();
                tx.executeWithoutResult(transaction -> {
                    Long activeLease = jdbc.queryForObject(
                            "SELECT next_check_ms FROM official_price_sync WHERE id=1 FOR UPDATE", Long.class);
                    if (activeLease == null || activeLease != lease) return;
                    LocalDate newest = jdbc.query("SELECT MAX(report_date) FROM official_price_reports",
                            (rs, n) -> rs.getDate(1) == null ? null : rs.getDate(1).toLocalDate()).get(0);
                    if (newest != null && report.reportDate().isBefore(newest)) {
                        throw new IllegalStateException("Downloaded report is older than the saved report");
                    }
                    if (report.prices().isEmpty() || report.reportDate().isAfter(LocalDate.now(ZONE))) {
                        throw new IllegalStateException("Invalid report snapshot");
                    }
                    // Same-date corrections are replaced atomically. Other report dates survive.
                    jdbc.update("""
                        INSERT INTO official_price_reports(report_date,fetched_ms,source_url)
                        VALUES (?,?,?) ON DUPLICATE KEY UPDATE fetched_ms=?,source_url=?
                        """, report.reportDate(), report.fetchedAt().toEpochMilli(), report.sourceUrl(),
                            report.fetchedAt().toEpochMilli(), report.sourceUrl());
                    jdbc.update("DELETE FROM official_price_values WHERE report_date=?", report.reportDate());
                    for (var price : report.prices()) {
                        jdbc.update("""
                            INSERT INTO official_price_values(report_date,commodity,market,price_lkr_per_kg)
                            VALUES (?,?,?,?)
                            """, report.reportDate(), price.commodity(), price.market(), price.priceLkrPerKg());
                    }
                    long completed = System.currentTimeMillis();
                    jdbc.update("""
                        UPDATE official_price_sync SET last_checked_ms=?,last_success_ms=?,
                        next_check_ms=?,last_error=NULL WHERE id=1
                        """, completed, completed, completed + CHECK_INTERVAL_MS);
                    log.info("Saved CBSL report {} with {} wholesale prices", report.reportDate(), report.prices().size());
                });
            } catch (Exception ex) {
                long completed = System.currentTimeMillis();
                jdbc.update("""
                    UPDATE official_price_sync SET last_checked_ms=?,next_check_ms=?,last_error=?
                    WHERE id=1 AND next_check_ms=?
                    """, completed, completed + RETRY_MS,
                        "Source update failed. Last saved prices are retained; retrying in five minutes.", lease);
                log.warn("Official price update failed; saved prices retained: {}", ex.toString());
            }
        } catch (Exception ex) {
            log.error("Official price database check failed", ex);
        }
    }

    public Result latest() {
        return tx.execute(transaction -> {
            var sync = jdbc.queryForMap("SELECT * FROM official_price_sync WHERE id=1");
            Instant checked = instant(sync.get("last_checked_ms"));
            Instant success = instant(sync.get("last_success_ms"));
            String warning = (String) sync.get("last_error");
            if (checked != null && checked.isBefore(Instant.now().minusSeconds(7_500))) {
                warning = "Scheduled check is overdue. Displaying the last saved prices.";
            }
            var reports = jdbc.query("""
                SELECT report_date,fetched_ms,source_url FROM official_price_reports
                ORDER BY report_date DESC LIMIT 1
                """, (rs, n) -> new OfficialPriceSource.Snapshot(rs.getDate(1).toLocalDate(),
                    Instant.ofEpochMilli(rs.getLong(2)), rs.getString(3), List.of()));
            if (reports.isEmpty()) {
                return new Result("UNAVAILABLE", null, null, checked, success, INDEX,
                        warning == null ? "Waiting for the first successful automatic update." : warning, List.of());
            }
            var report = reports.get(0);
            // n.a. for a market today may still have a valid earlier observation.
            // Keep its ORIGINAL date and source, never label it as today's price.
            var prices = jdbc.query("""
                SELECT p.commodity,p.market,p.price_lkr_per_kg,p.report_date,r.source_url
                FROM official_price_values p
                JOIN official_price_reports r ON r.report_date=p.report_date
                WHERE NOT EXISTS (
                    SELECT 1 FROM official_price_values newer
                    WHERE newer.commodity=p.commodity AND newer.market=p.market
                      AND newer.report_date>p.report_date
                )
                ORDER BY p.commodity,p.market
                """, (rs, n) -> new SavedPrice(rs.getString(1), rs.getString(2),
                    rs.getBigDecimal(3), rs.getDate(4).toLocalDate(), rs.getString(5)));
            String status = warning != null ? "CACHED"
                    : report.reportDate().isBefore(LocalDate.now(ZONE)) ? "PREVIOUS_REPORT" : "LATEST_REPORT";
            return new Result(status, report.reportDate(), report.fetchedAt(), checked, success,
                    report.sourceUrl(), warning, prices);
        });
    }

    private static Instant instant(Object value) {
        return value == null ? null : Instant.ofEpochMilli(((Number) value).longValue());
    }
}
