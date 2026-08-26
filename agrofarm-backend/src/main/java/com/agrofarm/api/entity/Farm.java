package com.agrofarm.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "farms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "farm_name", nullable = false, length = 200)
    private String farmName;

    @Column(name = "farm_address", nullable = false, length = 200)
    private String farmAddress;

    @Column(
            name = "latitude",
            nullable = false,
            precision = 10,
            scale = 7
    )
    private BigDecimal latitude;

    @Column(
            name = "longtitude",
            nullable = false,
            precision = 11,
            scale = 7
    )
    private BigDecimal longtitude;

    @Column(
            name = "boundary",
            columnDefinition = "TEXT"
    )
    private String boundary;
}