package com.agrofarm.api.entity;

import com.agrofarm.api.util.Status;
import com.agrofarm.api.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    private Long id;
    private Long userId;
    private Long farmId;
    private String userName;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private BigDecimal checkInLatitude;
    private BigDecimal checkInLongitude;
    private LocalDateTime checkOutTime;
    private BigDecimal checkOutLatitude;
    private BigDecimal checkOutLongitude;
    private Status status;
    private String location;
}