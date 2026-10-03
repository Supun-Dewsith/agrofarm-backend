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
        @JsonProperty("farm_id") Long farmId,
        @JsonProperty("field_id") Long fieldId,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("plant_name") String plantName,
        @JsonProperty("is_healthy") Boolean isHealthy,
        @JsonProperty("disease_name") String diseaseName,
        @JsonProperty("overall_confidence") Double overallConfidence,
        @JsonProperty("general_description") String generalDescription,
        @JsonProperty("detected_spots") List<DetectedSpotContext> detectedSpots,
        @JsonProperty("possible_treatments") List<String> possibleTreatments,
        @JsonProperty("prevention_tips") List<String> preventionTips,

        // 1. Biological & Pathogen Profile
        @JsonProperty("pathogen_type") String pathogenType,
        @JsonProperty("scientific_name") String scientificName,
        @JsonProperty("optimal_temperature") String optimalTemperature,
        @JsonProperty("humidity_level") String humidityLevel,
        @JsonProperty("weather_correlation") String weatherCorrelation,
        @JsonProperty("life_cycle_stages") List<LifeCycleStageContext> lifeCycleStages,

        // 2. Yield & Economic Impact Forecast
        @JsonProperty("yield_loss_risk") String yieldLossRisk,
        @JsonProperty("yield_loss_percentage") Integer yieldLossPercentage,
        @JsonProperty("spread_speed") String spreadSpeed,
        @JsonProperty("harvest_impact") String harvestImpact,

        // 3. Integrated Pest Management (IPM) Matrix
        @JsonProperty("ipm_matrix") List<IpmTreatmentItemContext> ipmMatrix,

        // 4. Multi-class Probability Distribution
        @JsonProperty("probability_distribution") List<ClassProbabilityContext> probabilityDistribution,

        // 5. Reference Specimen URL / Tag
        @JsonProperty("reference_image_url") String referenceImageUrl,

        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
