package com.agrofarm.api.dto.dashboard;

public record AttendanceTrendDay(
        String day,
        String date,
        int present,
        int late,
        int absent
) {}
