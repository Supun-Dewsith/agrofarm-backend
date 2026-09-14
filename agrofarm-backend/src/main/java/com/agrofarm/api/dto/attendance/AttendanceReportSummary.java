package com.agrofarm.api.dto.attendance;

public record AttendanceReportSummary(
        Long userId,
        String userName,
        Long farmId,
        String farmName,
        long scheduledDays,
        long presentDays,
        long lateDays,
        long absentDays,
        long halfDays,
        long leaveDays,
        double workedHours
) {}