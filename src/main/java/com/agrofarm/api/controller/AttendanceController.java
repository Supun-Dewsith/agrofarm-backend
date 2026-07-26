package com.agrofarm.api.controller;

import com.agrofarm.api.dto.AttendanceDashboardResponse;
import com.agrofarm.api.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public ResponseEntity<AttendanceDashboardResponse> getDashboardData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AttendanceDashboardResponse response = attendanceService.getDashboardData(date);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark/{userId}")
    public ResponseEntity<String> markAttendance(@PathVariable Long userId) {
        String message = attendanceService.markAttendance(userId);
        return ResponseEntity.ok(message);
    }
}