package com.agrofarm.api.dto.crops;

import lombok.Data;

import java.util.List;

@Data
public class Fields {

    private Long id;
    private String field_name;
    private double field_size;
    private String size_unit;
}