package com.agrofarm.api.dto.inventory;


import lombok.Data;

@Data
public class newInventoryRequest {

    String name;
    String category;
    Double quantity;
    String unit;
    Double alertLimit;
    Integer farmIDs;


}
