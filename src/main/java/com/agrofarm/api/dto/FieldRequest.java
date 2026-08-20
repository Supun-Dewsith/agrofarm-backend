package com.agrofarm.api.dto;

import com.agrofarm.api.util.SizeUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldRequest {

        @NotNull
        private Long farmId;

        @NotBlank
        private String fieldName;

        @NotNull
        private BigDecimal fieldSize;

        @NotNull
        private SizeUnit sizeUnit;

        @NotNull
        private BigDecimal latitude;

        @NotNull
        private BigDecimal longtitude;

        @NotBlank
        private String boundary;
}