package com.agrofarm.api.dto.disease;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IpmTreatmentItemContext(
        @JsonProperty("category") String category,
        @JsonProperty("method") String method,
        @JsonProperty("dosage_and_timing") String dosageAndTiming,
        @JsonProperty("phi_interval") String phiInterval
) {}
