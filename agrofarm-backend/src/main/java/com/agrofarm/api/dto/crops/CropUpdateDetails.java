package com.agrofarm.api.dto.crops;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CropUpdateDetails {

    Long id;
    String name;
    String variety;
    Long farmId;
    Long fieldId;
    String growthStage;
    Integer progress;
    String status;
    LocalDateTime plantDate;
    LocalDateTime harvestDate;
    String image;

}
