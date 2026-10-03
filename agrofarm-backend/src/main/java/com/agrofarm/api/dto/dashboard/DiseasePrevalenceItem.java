package com.agrofarm.api.dto.dashboard;

public record DiseasePrevalenceItem(
        String diseaseName,
        String plantName,
        int detectionCount,
        double avgConfidence
) {}
