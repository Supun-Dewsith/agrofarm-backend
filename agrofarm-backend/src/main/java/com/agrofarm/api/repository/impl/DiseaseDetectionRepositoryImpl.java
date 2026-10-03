package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.disease.ClassProbabilityContext;
import com.agrofarm.api.dto.disease.DetectedSpotContext;
import com.agrofarm.api.dto.disease.DiseaseAnalysisContext;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;
import com.agrofarm.api.dto.disease.IpmTreatmentItemContext;
import com.agrofarm.api.dto.disease.LifeCycleStageContext;
import com.agrofarm.api.repository.DiseaseDetectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class DiseaseDetectionRepositoryImpl implements DiseaseDetectionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DiseaseDetectionRepositoryImpl(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Long save(Long userId, String imageUrl, DiseaseAnalysisContext context) {
        String sql = """
                INSERT INTO disease_detections (
                    user_id, farm_id, field_id, image_url, plant_name, disease_name, is_healthy,
                    overall_confidence, general_description, detected_spots,
                    possible_treatments, prevention_tips,
                    pathogen_type, scientific_name, optimal_temperature, humidity_level,
                    weather_correlation, life_cycle_stages, yield_loss_risk, yield_loss_percentage,
                    spread_speed, harvest_impact, ipm_matrix, probability_distribution, reference_image_url
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String spotsJson = safeSerialize(context.detectedSpots());
        String treatmentsJson = safeSerialize(context.possibleTreatments());
        String tipsJson = safeSerialize(context.preventionTips());
        String lifeCycleJson = safeSerialize(context.lifeCycleStages());
        String ipmJson = safeSerialize(context.ipmMatrix());
        String probJson = safeSerialize(context.probabilityDistribution());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setNull(2, java.sql.Types.BIGINT);
            ps.setNull(3, java.sql.Types.BIGINT);
            ps.setString(4, imageUrl);
            ps.setString(5, context.plantName() != null ? context.plantName() : "Unknown");
            ps.setString(6, context.diseaseName() != null ? context.diseaseName() : "Undetermined");
            ps.setBoolean(7, Boolean.TRUE.equals(context.isHealthy()));
            ps.setDouble(8, context.overallConfidence() != null ? context.overallConfidence() : 0.0);
            ps.setString(9, context.generalDescription());
            ps.setString(10, spotsJson);
            ps.setString(11, treatmentsJson);
            ps.setString(12, tipsJson);

            ps.setString(13, context.pathogenType());
            ps.setString(14, context.scientificName());
            ps.setString(15, context.optimalTemperature());
            ps.setString(16, context.humidityLevel());
            ps.setString(17, context.weatherCorrelation());
            ps.setString(18, lifeCycleJson);
            ps.setString(19, context.yieldLossRisk());
            ps.setInt(20, context.yieldLossPercentage() != null ? context.yieldLossPercentage() : 0);
            ps.setString(21, context.spreadSpeed());
            ps.setString(22, context.harvestImpact());
            ps.setString(23, ipmJson);
            ps.setString(24, probJson);
            ps.setString(25, context.referenceImageUrl());

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public List<DiseaseDetectionResponse> findRecentByUserId(Long userId, int limit) {
        String sql = """
                SELECT id, user_id, farm_id, field_id, image_url, plant_name, disease_name, is_healthy,
                       overall_confidence, general_description, detected_spots,
                       possible_treatments, prevention_tips,
                       pathogen_type, scientific_name, optimal_temperature, humidity_level,
                       weather_correlation, life_cycle_stages, yield_loss_risk, yield_loss_percentage,
                       spread_speed, harvest_impact, ipm_matrix, probability_distribution, reference_image_url,
                       created_at
                FROM disease_detections
                WHERE user_id = ?
                ORDER BY created_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToDiseaseDetection, userId, limit);
    }

    @Override
    public Optional<DiseaseDetectionResponse> findByIdAndUserId(Long id, Long userId) {
        String sql = """
                SELECT id, user_id, farm_id, field_id, image_url, plant_name, disease_name, is_healthy,
                       overall_confidence, general_description, detected_spots,
                       possible_treatments, prevention_tips,
                       pathogen_type, scientific_name, optimal_temperature, humidity_level,
                       weather_correlation, life_cycle_stages, yield_loss_risk, yield_loss_percentage,
                       spread_speed, harvest_impact, ipm_matrix, probability_distribution, reference_image_url,
                       created_at
                FROM disease_detections
                WHERE id = ? AND user_id = ?
                """;
        List<DiseaseDetectionResponse> results = jdbcTemplate.query(sql, this::mapRowToDiseaseDetection, id, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean linkFarmAndField(Long id, Long farmId, Long fieldId, Long userId) {
        String sql = "UPDATE disease_detections SET farm_id = ?, field_id = ? WHERE id = ? AND user_id = ?";
        int updated = jdbcTemplate.update(sql, farmId, fieldId, id, userId);
        return updated > 0;
    }

    private DiseaseDetectionResponse mapRowToDiseaseDetection(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        Long userId = rs.getLong("user_id");

        Long farmId = rs.getObject("farm_id") != null ? rs.getLong("farm_id") : null;
        Long fieldId = rs.getObject("field_id") != null ? rs.getLong("field_id") : null;

        String imageUrl = rs.getString("image_url");
        String plantName = rs.getString("plant_name");
        String diseaseName = rs.getString("disease_name");
        boolean isHealthy = rs.getBoolean("is_healthy");
        double overallConfidence = rs.getDouble("overall_confidence");
        String generalDescription = rs.getString("general_description");
        String spotsJson = rs.getString("detected_spots");
        String treatmentsJson = rs.getString("possible_treatments");
        String tipsJson = rs.getString("prevention_tips");

        String pathogenType = safeGetString(rs, "pathogen_type");
        String scientificName = safeGetString(rs, "scientific_name");
        String optimalTemperature = safeGetString(rs, "optimal_temperature");
        String humidityLevel = safeGetString(rs, "humidity_level");
        String weatherCorrelation = safeGetString(rs, "weather_correlation");
        String lifeCycleJson = safeGetString(rs, "life_cycle_stages");
        String yieldLossRisk = safeGetString(rs, "yield_loss_risk");
        Integer yieldLossPercentage = safeGetInt(rs, "yield_loss_percentage");
        String spreadSpeed = safeGetString(rs, "spread_speed");
        String harvestImpact = safeGetString(rs, "harvest_impact");
        String ipmJson = safeGetString(rs, "ipm_matrix");
        String probJson = safeGetString(rs, "probability_distribution");
        String referenceImageUrl = safeGetString(rs, "reference_image_url");

        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        List<DetectedSpotContext> detectedSpots = parseList(spotsJson, new TypeReference<List<DetectedSpotContext>>() {});
        List<String> possibleTreatments = parseList(treatmentsJson, new TypeReference<List<String>>() {});
        List<String> preventionTips = parseList(tipsJson, new TypeReference<List<String>>() {});
        List<LifeCycleStageContext> lifeCycleStages = parseList(lifeCycleJson, new TypeReference<List<LifeCycleStageContext>>() {});
        List<IpmTreatmentItemContext> ipmMatrix = parseList(ipmJson, new TypeReference<List<IpmTreatmentItemContext>>() {});
        List<ClassProbabilityContext> probabilityDistribution = parseList(probJson, new TypeReference<List<ClassProbabilityContext>>() {});

        return new DiseaseDetectionResponse(
                id,
                userId,
                farmId,
                fieldId,
                imageUrl,
                plantName,
                isHealthy,
                diseaseName,
                overallConfidence,
                generalDescription,
                detectedSpots,
                possibleTreatments,
                preventionTips,
                pathogenType,
                scientificName,
                optimalTemperature,
                humidityLevel,
                weatherCorrelation,
                lifeCycleStages,
                yieldLossRisk,
                yieldLossPercentage,
                spreadSpeed,
                harvestImpact,
                ipmMatrix,
                probabilityDistribution,
                referenceImageUrl,
                createdAt
        );
    }

    private String safeGetString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer safeGetInt(ResultSet rs, String column) {
        try {
            int val = rs.getInt(column);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }

    private String safeSerialize(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> typeRef) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
