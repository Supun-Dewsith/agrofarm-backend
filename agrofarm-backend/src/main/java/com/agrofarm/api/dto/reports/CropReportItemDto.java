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
public class CropReportItemDto {
    private Long cropId;
    private String cropName;
    private String variety;
    private Long farmId;
    private String farmName;
    private Long fieldId;
    private String fieldName;
    private String growthStage;
    private Integer progress;
    private String status;
    private LocalDateTime plantDate;
    private LocalDateTime harvestDate;
}
