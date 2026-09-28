package com.agrofarm.api.repository.impl;

import com.agrofarm.api.dto.disease.DetectedSpotContext;
import com.agrofarm.api.dto.disease.DiseaseAnalysisContext;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;
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
                    user_id, image_url, plant_name, disease_name, is_healthy,
                    overall_confidence, general_description, detected_spots,
                    possible_treatments, prevention_tips
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String spotsJson = safeSerialize(context.detectedSpots());
        String treatmentsJson = safeSerialize(context.possibleTreatments());
        String tipsJson = safeSerialize(context.preventionTips());

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, imageUrl);
            ps.setString(3, context.plantName() != null ? context.plantName() : "Unknown");
            ps.setString(4, context.diseaseName() != null ? context.diseaseName() : "Undetermined");
            ps.setBoolean(5, Boolean.TRUE.equals(context.isHealthy()));
            ps.setDouble(6, context.overallConfidence() != null ? context.overallConfidence() : 0.0);
            ps.setString(7, context.generalDescription());
            ps.setString(8, spotsJson);
            ps.setString(9, treatmentsJson);
            ps.setString(10, tipsJson);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public List<DiseaseDetectionResponse> findRecentByUserId(Long userId, int limit) {
        String sql = """
                SELECT id, user_id, image_url, plant_name, disease_name, is_healthy,
                       overall_confidence, general_description, detected_spots,
                       possible_treatments, prevention_tips, created_at
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
                SELECT id, user_id, image_url, plant_name, disease_name, is_healthy,
                       overall_confidence, general_description, detected_spots,
                       possible_treatments, prevention_tips, created_at
                FROM disease_detections
                WHERE id = ? AND user_id = ?
                """;
        List<DiseaseDetectionResponse> results = jdbcTemplate.query(sql, this::mapRowToDiseaseDetection, id, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private DiseaseDetectionResponse mapRowToDiseaseDetection(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        Long userId = rs.getLong("user_id");
        String imageUrl = rs.getString("image_url");
        String plantName = rs.getString("plant_name");
        String diseaseName = rs.getString("disease_name");
        boolean isHealthy = rs.getBoolean("is_healthy");
        double overallConfidence = rs.getDouble("overall_confidence");
        String generalDescription = rs.getString("general_description");
        String spotsJson = rs.getString("detected_spots");
        String treatmentsJson = rs.getString("possible_treatments");
        String tipsJson = rs.getString("prevention_tips");
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;

        List<DetectedSpotContext> detectedSpots = parseList(spotsJson, new TypeReference<List<DetectedSpotContext>>() {});
        List<String> possibleTreatments = parseList(treatmentsJson, new TypeReference<List<String>>() {});
        List<String> preventionTips = parseList(tipsJson, new TypeReference<List<String>>() {});

        return new DiseaseDetectionResponse(
                id,
                userId,
                imageUrl,
                plantName,
                isHealthy,
                diseaseName,
                overallConfidence,
                generalDescription,
                detectedSpots,
                possibleTreatments,
                preventionTips,
                createdAt
        );
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
