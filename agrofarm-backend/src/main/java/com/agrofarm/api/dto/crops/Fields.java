package com.agrofarm.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class Fields {

    private Long id;
    private String field_name;
    private decimal field_size;
    private String size_unit;
}