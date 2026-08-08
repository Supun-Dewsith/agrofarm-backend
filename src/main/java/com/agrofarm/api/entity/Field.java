package com.agrofarm.api.entity;

import com.agrofarm.api.util.SizeUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_size", nullable = false, precision = 10, scale = 2)
    private BigDecimal fieldSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "size_unit", nullable = false)
    private SizeUnit sizeUnit;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longtitude", nullable = false, precision = 11, scale = 7)
    private BigDecimal longtitude;
}