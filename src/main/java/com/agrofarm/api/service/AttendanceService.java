package com.agrofarm.api.service;

import com.agrofarm.api.dto.AttendanceDashboardResponse;
import java.time.LocalDate;

public interface AttendanceService {
    AttendanceDashboardResponse getDashboardData(LocalDate date);
    String markAttendance(Long userId);
}