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


        @NotBlank(message = "Farm name is required")
        private String farmName;


        @NotBlank(message = "Farm address is required")
        private String farmAddress;


        @NotNull(message = "Latitude is required")
        private BigDecimal latitude;


        @NotNull(message = "Longtitude is required")
        private BigDecimal longtitude;


        @NotBlank(message = "Boundary is required")
        private String boundary;

}