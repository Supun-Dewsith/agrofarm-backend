package com.agrofarm.api.dto.crops;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CropDetails {

    Long id;
    String name;
    String variety;
    String image;
    String field;
    String area;
    String growthStage;
    Integer progress;
    String status;
    LocalDateTime plantDate;
    LocalDateTime harvestDate;

}
