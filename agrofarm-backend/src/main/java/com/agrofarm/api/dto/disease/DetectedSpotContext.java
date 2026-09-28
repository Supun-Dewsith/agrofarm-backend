package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DetectedSpotContext(
        @JsonProperty("box_2d") List<Integer> box2d,
        double confidence,
        String description
) {}