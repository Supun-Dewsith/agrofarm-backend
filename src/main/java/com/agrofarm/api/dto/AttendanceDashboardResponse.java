package com.agrofarm.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AttendanceDashboardResponse {
    private int total;
    private int present;
    private int late;
    private int absent;
    private List<AttendanceResponse> todayAttendance;
}