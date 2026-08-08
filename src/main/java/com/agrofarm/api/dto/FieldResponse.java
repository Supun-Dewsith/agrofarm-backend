package com.agrofarm.api.dto;

import com.agrofarm.api.util.SizeUnit;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldResponse {

    private Long id;

    private Long farmId;

    private String fieldName;

    private BigDecimal fieldSize;

    private SizeUnit sizeUnit;

    private BigDecimal latitude;

    private BigDecimal longtitude;
}