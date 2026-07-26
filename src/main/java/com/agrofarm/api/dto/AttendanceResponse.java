package com.agrofarm.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private String employeeName;
    private String role;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private String status;
}
