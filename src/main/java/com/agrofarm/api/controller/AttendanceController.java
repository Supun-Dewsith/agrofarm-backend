package com.agrofarm.api.controller;

import com.agrofarm.api.dto.AttendanceRequest;
import com.agrofarm.api.dto.AttendanceResponse;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;
import com.agrofarm.api.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Employee Check-In
     */
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponse> checkIn(
            @RequestBody AttendanceRequest request) {

        AttendanceResponse response = attendanceService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Employee Check-Out
     */
    @PutMapping("/check-out/{userId}")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long userId) {

        AttendanceResponse response = attendanceService.checkOut(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * QR Attendance
     */
    @PostMapping("/qr-scan")
    public ResponseEntity<QrAttendanceResponse> processQrAttendance(
            @RequestBody QrAttendanceRequest request) {

        QrAttendanceResponse response =
                attendanceService.processQrAttendance(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard Summary
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AttendanceDashboardResponse> dashboard(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        AttendanceDashboardResponse response =
                attendanceService.getDashboardSummary(date);

        return ResponseEntity.ok(response);
    }

    /**
     * Attendance By Date
     */
    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDate(date)
        );
    }

    /**
     * Employee Attendance History
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<AttendanceResponse>> getHistory(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceHistory(userId)
        );
    }

}