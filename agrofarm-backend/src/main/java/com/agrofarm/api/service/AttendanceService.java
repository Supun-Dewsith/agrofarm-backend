package com.agrofarm.api.service;

import com.agrofarm.api.dto.AttendanceCodeRequest;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.*;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    List<AttendanceFarmResponse> getFarms();
    List<AttendanceWorkerResponse> getWorkers(Long farmId, String search);
    List<AttendanceResponse> getDailyAttendance(LocalDate date, Long farmId, String search);
    AttendanceDashboardResponse getDashboard(LocalDate date, Long farmId);
    QrGenerateResponse generateQr(Long farmId);
    QrLiveCodeResponse getQrLiveCode(String token);
    QrAttendanceResponse processQrAttendance(QrAttendanceRequest request);
    QrAttendanceResponse processCodeAttendance(AttendanceCodeRequest request);
    List<AttendanceResponse> getHistory(Long userId);
    AttendanceResponse checkOut(Long userId, String location, Double latitude, Double longitude, Double accuracyMeters);
    List<AttendanceResponse> getReport(LocalDate from, LocalDate to, Long farmId, Long userId, String search);
    List<AttendanceReportSummary> getReportSummary(LocalDate from, LocalDate to, Long farmId, Long userId, String search);
}
