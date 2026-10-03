package com.agrofarm.api.dto.attendance;

import java.util.List;

public record AttendanceDashboardResponse(
        long totalWorkers,
        long presentToday,
        long lateToday,
        long absentToday,
        List<AttendanceResponse> recentAttendances
) {}