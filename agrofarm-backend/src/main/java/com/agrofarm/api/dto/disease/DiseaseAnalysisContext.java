package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DiseaseAnalysisContext(
        @JsonProperty("plant_name") String plantName,
        @JsonProperty("is_healthy") Boolean isHealthy,
        @JsonProperty("disease_name") String diseaseName,
        @JsonProperty("overall_confidence") Double overallConfidence,
        @JsonProperty("general_description") String generalDescription,
        @JsonProperty("detected_spots") List<DetectedSpotContext> detectedSpots,
        @JsonProperty("possible_treatments") List<String> possibleTreatments,
        @JsonProperty("prevention_tips") List<String> preventionTips
) {}