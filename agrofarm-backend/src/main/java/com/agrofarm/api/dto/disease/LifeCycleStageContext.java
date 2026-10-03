package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LifeCycleStageContext(
        @JsonProperty("stage_name") String stageName,
        @JsonProperty("duration") String duration,
        @JsonProperty("description") String description
) {}
