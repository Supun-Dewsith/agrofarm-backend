package com.agrofarm.api.dto.crops;

import lombok.Data;

import java.util.Date;


@Data
public class NewCropRequest {

    String cropName;
    String variety;
    Long farmId;
    Long fieldId;
    String growthStage;
    Number progress;
    String status;
    Date plantDate;
    Date harvestDate;
}
