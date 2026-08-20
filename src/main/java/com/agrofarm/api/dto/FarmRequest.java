package com.agrofarm.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmRequest {

        @NotNull
        private Long ownerId;

        @NotBlank
        private String farmName;

        @NotBlank
        private String farmAddress;

        @NotNull
        private BigDecimal latitude;

        @NotNull
        private BigDecimal longtitude;

        @NotBlank
        private String boundary;
}