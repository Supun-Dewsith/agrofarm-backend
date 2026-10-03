package com.agrofarm.api.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportItemDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long farmId;
    private String farmName;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double hoursWorked;
    private String status;
    private String location;
    private Boolean verified;
}
