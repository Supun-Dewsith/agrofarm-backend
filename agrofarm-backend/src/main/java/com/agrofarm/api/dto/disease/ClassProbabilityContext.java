package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClassProbabilityContext(
        @JsonProperty("name") String name,
        @JsonProperty("probability") Double probability
) {}
