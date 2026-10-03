package com.agrofarm.api.dto.inventory;


import lombok.Data;

@Data
public class InventoryRequest {

    private Integer id;
    private String name;
    private String category;
    private Double quantity;
    private String unit;
    private Double alertLimit;
    private Long farmIds;

}
