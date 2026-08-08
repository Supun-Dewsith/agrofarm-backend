package com.agrofarm.api.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmResponse {

    private Long id;

    private Long ownerId;

    private String farmName;

    private String farmAddress;

    private BigDecimal latitude;

    private BigDecimal longtitude;
}