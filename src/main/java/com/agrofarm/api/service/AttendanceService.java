package com.agrofarm.api.service;

import com.agrofarm.api.dto.AttendanceRequest;
import com.agrofarm.api.dto.AttendanceResponse;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    /**
     * Employee Check-In
     */
    AttendanceResponse checkIn(AttendanceRequest request);

    /**
     * Employee Check-Out
     */
    AttendanceResponse checkOut(Long userId);

    /**
     * QR Attendance
     */
    QrAttendanceResponse processQrAttendance(QrAttendanceRequest request);

    /**
     * Dashboard Summary
     */
    AttendanceDashboardResponse getDashboardSummary(LocalDate date);

    /**
     * Today's Attendance List
     */
    List<AttendanceResponse> getAttendanceByDate(LocalDate date);

    /**
     * Employee Attendance History
     */
    List<AttendanceResponse> getAttendanceHistory(Long userId);

}