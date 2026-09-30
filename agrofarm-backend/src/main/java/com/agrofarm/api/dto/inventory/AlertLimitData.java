package com.agrofarm.api.dto.inventory;


import lombok.Data;

@Data
public class AlertLimitData {

    Integer id;
    String name;
    String current;
    String reorderAt;
    Integer farmId;
    String category;
}
