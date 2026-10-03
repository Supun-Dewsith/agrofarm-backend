package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiseaseDetectionResponse(
        Long id,
        @JsonProperty("user_id") Long userId,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("plant_name") String plantName,
        @JsonProperty("is_healthy") Boolean isHealthy,
        @JsonProperty("disease_name") String diseaseName,
        @JsonProperty("overall_confidence") Double overallConfidence,
        @JsonProperty("general_description") String generalDescription,
        @JsonProperty("detected_spots") List<DetectedSpotContext> detectedSpots,
        @JsonProperty("possible_treatments") List<String> possibleTreatments,
        @JsonProperty("prevention_tips") List<String> preventionTips,
        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
