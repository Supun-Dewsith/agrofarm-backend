package com.agrofarm.api.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseReportItemDto {
    private Long id;
    private Long userId;
    private String userName;
    private String plantName;
    private String diseaseName;
    private Boolean isHealthy;
    private Double confidence;
    private String treatment;
    private LocalDateTime detectedAt;
}
