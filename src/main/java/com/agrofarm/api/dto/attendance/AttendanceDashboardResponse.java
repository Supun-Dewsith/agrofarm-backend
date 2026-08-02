package com.agrofarm.api.dto.attendance;

import com.agrofarm.api.dto.AttendanceResponse;

import java.util.List;

public record AttendanceDashboardResponse(

        long totalWorkers,

        long presentToday,

        long lateToday,

        long absentToday,

        List<AttendanceResponse> recentAttendances

) {
}