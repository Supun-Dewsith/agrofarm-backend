package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.HarvestRequest;
import com.agrofarm.api.dto.MarketPriceRequest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class HarvestServiceImpl {

    private static final ZoneId ZONE = ZoneId.of("Asia/Colombo");

    // Every query using this condition must use "f" as the farms alias.
    private static final String FARM_ACCESS = """
            (f.owner_id = ? OR EXISTS (
                SELECT 1
                FROM users access_user
                WHERE access_user.id = ?
                AND (
                    access_user.role = 'ADMIN'
                    OR EXISTS (
                        SELECT 1
                        FROM farm_assignments fa
                        WHERE fa.farm_id = f.id
                          AND fa.user_id = access_user.id
                    )
                )
            ))
            """;

    private final JdbcTemplate jdbc;

    public HarvestServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> getCropOptions(String username) {
        long userId = userId(username);

        return jdbc.queryForList(
                """
                SELECT c.crop_id AS cropId,
                       c.crop_name AS cropName,
                       c.variety,
                       c.farm_id AS farmId,
                       f.farm_name AS farmName,
                       c.field_id AS fieldId,
                       fi.field_name AS fieldName,
                       c.plant_date AS plantDate,
                       c.harvest_date AS expectedHarvestDate,
                       c.growth_stage AS growthStage,
                       c.progress,
                       c.status,
                       f.latitude AS farmLatitude,
                       f.longtitude AS farmLongitude
                FROM crops c
                JOIN farms f ON f.id = c.farm_id
                LEFT JOIN fields fi ON fi.id = c.field_id
                WHERE
                """ + FARM_ACCESS + """
                ORDER BY c.harvest_date IS NULL, c.harvest_date, c.crop_name
                """,
                userId,
                userId
        );
    }

    public List<Map<String, Object>> getRecords(String username) {
        long userId = userId(username);

        return jdbc.queryForList(
                """
                SELECT h.id,
                       h.crop_id AS cropId,
                       c.crop_name AS cropName,
                       c.variety,
                       f.id AS farmId,
                       f.farm_name AS farmName,
                       fi.id AS fieldId,
                       fi.field_name AS fieldName,
                       h.harvested_on AS harvestedOn,
                       h.quantity_kg AS quantityKg,
                       h.grade,
                       h.unit_price_lkr_per_kg AS unitPriceLkrPerKg,
                       h.sold_market AS soldMarket,
                       h.notes,
                       h.created_at AS createdAt
                FROM harvest_records h
                JOIN crops c ON c.crop_id = h.crop_id
                JOIN farms f ON f.id = c.farm_id
                LEFT JOIN fields fi ON fi.id = c.field_id
                WHERE
                """ + FARM_ACCESS + """
                ORDER BY h.harvested_on DESC, h.id DESC
                """,
                userId,
                userId
        );
    }

    @Transactional
    public Map<String, Object> createRecord(
            String username,
            HarvestRequest request
    ) {
        validateHarvest(request);

        long userId = userId(username);
        requireAccessibleCrop(request.cropId(), userId);

        KeyHolder key = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO harvest_records(
                        crop_id,
                        harvested_on,
                        quantity_kg,
                        grade,
                        unit_price_lkr_per_kg,
                        sold_market,
                        notes,
                        created_by
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, request.cropId());
            ps.setObject(2, request.harvestedOn());
            ps.setBigDecimal(3, request.quantityKg());
            ps.setString(4, clean(request.grade(), 30));
            ps.setBigDecimal(5, request.unitPriceLkrPerKg());
            ps.setString(6, clean(request.soldMarket(), 150));
            ps.setString(7, clean(request.notes(), 1000));
            ps.setLong(8, userId);

            return ps;
        }, key);

        return Map.of(
                "id", Objects.requireNonNull(key.getKey()).longValue(),
                "message", "Harvest record saved."
        );
    }

    @Transactional
    public Map<String, Object> updateRecord(
            String username,
            long id,
            HarvestRequest request
    ) {
        validateHarvest(request);

        long userId = userId(username);
        requireAccessibleCrop(request.cropId(), userId);

        int count = jdbc.update(
                """
                UPDATE harvest_records h
                JOIN crops existing_crop
                    ON existing_crop.crop_id = h.crop_id
                JOIN farms f
                    ON f.id = existing_crop.farm_id
                SET h.crop_id = ?,
                    h.harvested_on = ?,
                    h.quantity_kg = ?,
                    h.grade = ?,
                    h.unit_price_lkr_per_kg = ?,
                    h.sold_market = ?,
                    h.notes = ?
                WHERE h.id = ? AND
                """ + FARM_ACCESS,
                request.cropId(),
                request.harvestedOn(),
                request.quantityKg(),
                clean(request.grade(), 30),
                request.unitPriceLkrPerKg(),
                clean(request.soldMarket(), 150),
                clean(request.notes(), 1000),
                id,
                userId,
                userId
        );

        if (count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Harvest record not found."
            );
        }

        return Map.of(
                "id", id,
                "message", "Harvest record updated."
        );
    }

    public void deleteRecord(String username, long id) {
        long userId = userId(username);

        int count = jdbc.update(
                """
                DELETE h
                FROM harvest_records h
                JOIN crops c ON c.crop_id = h.crop_id
                JOIN farms f ON f.id = c.farm_id
                WHERE h.id = ? AND
                """ + FARM_ACCESS,
                id,
                userId,
                userId
        );

        if (count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Harvest record not found."
            );
        }
    }

    public Map<String, Object> getSummary(String username) {
        long userId = userId(username);

        return jdbc.queryForMap(
                """
                SELECT COALESCE(SUM(h.quantity_kg), 0) AS totalQuantityKg,
                       COUNT(DISTINCT h.crop_id) AS cropCount,
                       COUNT(*) AS recordCount,
                       COALESCE(
                           SUM(
                               h.quantity_kg *
                               COALESCE(h.unit_price_lkr_per_kg, 0)
                           ),
                           0
                       ) AS recordedSalesLkr
                FROM harvest_records h
                JOIN crops c ON c.crop_id = h.crop_id
                JOIN farms f ON f.id = c.farm_id
                WHERE
                """ + FARM_ACCESS + """
                AND h.harvested_on >= ?
                AND h.harvested_on <= ?
                """,
                userId,
                userId,
                LocalDate.now(ZONE).minusDays(29),
                LocalDate.now(ZONE)
        );
    }

    public List<Map<String, Object>> getMarketRecommendations(
            String username,
            long cropId
    ) {
        long userId = userId(username);
        requireAccessibleCrop(cropId, userId);

        Map<String, Object> crop = jdbc.queryForMap(
                """
                SELECT c.crop_name AS cropName,
                       f.latitude AS latitude,
                       f.longtitude AS longitude
                FROM crops c
                JOIN farms f ON f.id = c.farm_id
                WHERE c.crop_id = ?
                """,
                cropId
        );

        String commodity = String.valueOf(crop.get("cropName"));

        boolean hasOrigin =
                crop.get("latitude") instanceof Number
                        && crop.get("longitude") instanceof Number;

        double originLat = hasOrigin
                ? ((Number) crop.get("latitude")).doubleValue() : 0;

        double originLng = hasOrigin
                ? ((Number) crop.get("longitude")).doubleValue() : 0;

        List<Map<String, Object>> prices = jdbc.queryForList(
                """
                SELECT p.id,
                       p.commodity_name AS commodityName,
                       p.market_name AS marketName,
                       p.observed_on AS observedOn,
                       p.price_lkr_per_kg AS priceLkrPerKg,
                       DATE_FORMAT(
                           p.created_at,
                           '%Y-%m-%dT%H:%i:%s'
                       ) AS updatedAt,
                       p.price_type AS priceType,
                       p.source_name AS sourceName,
                       p.source_url AS sourceUrl,
                       p.market_latitude AS marketLatitude,
                       p.market_longitude AS marketLongitude
                FROM harvest_market_prices p
                WHERE LOWER(p.commodity_name) = LOWER(?)
                  AND p.observed_on >= ?
                  AND p.observed_on <= ?
                ORDER BY p.market_name, p.observed_on DESC, p.id DESC
                """,
                commodity,
                LocalDate.now(ZONE).minusDays(29),
                LocalDate.now(ZONE)
        );

        Map<String, List<Map<String, Object>>> byMarket =
                new LinkedHashMap<>();

        for (Map<String, Object> price : prices) {
            String key = String.valueOf(price.get("marketName"))
                    .toLowerCase(Locale.ROOT)
                    + "|" + price.get("priceType");

            byMarket.computeIfAbsent(
                    key,
                    ignored -> new ArrayList<>()
            ).add(price);
        }

        List<Map<String, Object>> latestByMarket = new ArrayList<>();

        for (List<Map<String, Object>> observations : byMarket.values()) {
            Map<String, Object> latest =
                    new LinkedHashMap<>(observations.get(0));

            Object lat = latest.get("marketLatitude");
            Object lng = latest.get("marketLongitude");

            double distance = Double.NaN;

            if (hasOrigin && lat instanceof Number a && lng instanceof Number b) {
                distance = haversineKm(
                        originLat,
                        originLng,
                        a.doubleValue(),
                        b.doubleValue()
                );
            }

            latest.put(
                    "distanceKm",
                    Double.isNaN(distance)
                            ? null
                            : BigDecimal.valueOf(distance)
                            .setScale(1, RoundingMode.HALF_UP)
            );

            latest.put(
                    "googleMapsUrl",
                    hasOrigin && lat instanceof Number a && lng instanceof Number b
                            ? "https://www.google.com/maps/dir/?api=1&origin="
                            + originLat + "," + originLng
                            + "&destination="
                            + a.doubleValue() + "," + b.doubleValue()
                            + "&travelmode=driving"
                            : null
            );

            LocalDate latestDate = asLocalDate(latest.get("observedOn"));

            latest.put(
                    "daysOld",
                    Math.max(
                            0,
                            LocalDate.now(ZONE).toEpochDay()
                                    - latestDate.toEpochDay()
                    )
            );

            if (observations.size() > 1) {
                Map<String, Object> previous = observations.get(1);

                BigDecimal previousPrice =
                        (BigDecimal) previous.get("priceLkrPerKg");

                BigDecimal currentPrice =
                        (BigDecimal) latest.get("priceLkrPerKg");

                if (previousPrice.signum() > 0) {
                    BigDecimal change = currentPrice
                            .subtract(previousPrice)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(previousPrice, 1, RoundingMode.HALF_UP);

                    latest.put("changeSincePreviousPercent", change);
                    latest.put(
                            "previousObservedOn",
                            previous.get("observedOn")
                    );
                }
            }

            latestByMarket.add(latest);
        }

        latestByMarket.sort(
                (left, right) ->
                        ((BigDecimal) right.get("priceLkrPerKg"))
                                .compareTo(
                                        (BigDecimal) left.get("priceLkrPerKg")
                                )
        );

        return latestByMarket;
    }

    @Transactional
    public Map<String, Object> addMarketPrice(
            String username,
            String role,
            MarketPriceRequest request
    ) {
        if (!("ADMIN".equals(role) || "SUPERVISOR".equals(role))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only an administrator or supervisor can update shared market prices."
            );
        }

        if (request == null
                || request.cropId() == null
                || request.marketName() == null
                || request.marketName().isBlank()
                || request.observedOn() == null
                || request.priceLkrPerKg() == null
                || request.priceLkrPerKg().signum() <= 0
                || request.sourceName() == null
                || request.sourceName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Crop, market, observation date, positive price and source are required."
            );
        }

        boolean onlyOneCoordinate =
                (request.marketLatitude() == null)
                        != (request.marketLongitude() == null);

        if (onlyOneCoordinate) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide both market latitude and longitude."
            );
        }

        if (request.marketLatitude() != null
                && (
                request.marketLatitude().abs()
                        .compareTo(BigDecimal.valueOf(90)) > 0
                        || request.marketLongitude().abs()
                        .compareTo(BigDecimal.valueOf(180)) > 0
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide valid market coordinates."
            );
        }

        if (request.marketName().trim().length() > 150
                || request.sourceName().trim().length() > 120) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Market name or source name is too long."
            );
        }

        if (request.observedOn().isAfter(LocalDate.now(ZONE))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Observation date cannot be in the future."
            );
        }

        String priceType = request.priceType() == null
                || request.priceType().isBlank()
                ? "WHOLESALE"
                : request.priceType().trim().toUpperCase(Locale.ROOT);

        if (!Set.of("WHOLESALE", "FARMGATE", "RETAIL").contains(priceType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Price type must be WHOLESALE, FARMGATE, or RETAIL."
            );
        }

        if (request.sourceUrl() != null
                && !request.sourceUrl().isBlank()
                && !request.sourceUrl().matches("(?i)^https://.+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source link must start with https://"
            );
        }

        long userId = userId(username);
        String commodity = requireAccessibleCrop(request.cropId(), userId);

        jdbc.update(
                """
                INSERT INTO harvest_market_prices(
                    commodity_name,
                    market_name,
                    observed_on,
                    price_lkr_per_kg,
                    price_type,
                    source_name,
                    source_url,
                    market_latitude,
                    market_longitude,
                    entered_by
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                commodity,
                request.marketName().trim(),
                request.observedOn(),
                request.priceLkrPerKg(),
                priceType,
                request.sourceName().trim(),
                clean(request.sourceUrl(), 500),
                request.marketLatitude(),
                request.marketLongitude(),
                userId
        );

        return Map.of(
                "message",
                "Market price added with its source and observation date."
        );
    }

    private void validateHarvest(HarvestRequest request) {
        if (request == null
                || request.cropId() == null
                || request.harvestedOn() == null
                || request.quantityKg() == null
                || request.quantityKg().signum() <= 0
                || (
                request.unitPriceLkrPerKg() != null
                        && request.unitPriceLkrPerKg().signum() <= 0
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Crop, harvest date and a positive quantity are required."
            );
        }

        if (request.harvestedOn().isAfter(LocalDate.now(ZONE))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Harvest date cannot be in the future."
            );
        }
    }

    private String requireAccessibleCrop(long cropId, long userId) {
        List<String> names = jdbc.query(
                """
                SELECT c.crop_name
                FROM crops c
                JOIN farms f ON f.id = c.farm_id
                WHERE c.crop_id = ? AND
                """ + FARM_ACCESS,
                (rs, n) -> rs.getString(1),
                cropId,
                userId,
                userId
        );

        if (names.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Crop not found in your assigned farms."
            );
        }

        return names.get(0);
    }

    private long userId(String username) {
        try {
            Long id = jdbc.queryForObject(
                    "SELECT id FROM users WHERE user_name = ?",
                    Long.class,
                    username
            );

            if (id == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found."
                );
            }

            return id;
        } catch (EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not found."
            );
        }
    }

    private static String clean(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.length() > max) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Text exceeds maximum length of " + max + "."
            );
        }

        return trimmed;
    }

    private static LocalDate asLocalDate(Object value) {
        return value instanceof java.sql.Date date
                ? date.toLocalDate()
                : LocalDate.parse(String.valueOf(value));
    }

    private static double haversineKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        a = Math.max(0, Math.min(1, a));

        return 6371.0088 * 2
                * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
