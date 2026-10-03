package com.agrofarm.api.repository;

import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;
import com.agrofarm.api.dto.attendance.AttendanceFarmResponse;
import com.agrofarm.api.dto.attendance.AttendanceReportSummary;
import com.agrofarm.api.dto.attendance.AttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceWorkerResponse;
import com.agrofarm.api.dto.attendance.AttendanceQrSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {

    List<AttendanceFarmResponse> findFarmsForUser(Long viewerUserId, String viewerRole);

    boolean canAccessFarm(Long viewerUserId, String viewerRole, Long farmId);

    boolean canAccessUser(Long viewerUserId, String viewerRole, Long targetUserId);

    List<AttendanceWorkerResponse> findWorkers(
            Long viewerUserId,
            String viewerRole,
            Long farmId,
            String search
    );

    List<AttendanceResponse> findDailyAttendance(
            Long viewerUserId,
            String viewerRole,
            LocalDate date,
            Long farmId,
            String search
    );

    AttendanceDashboardResponse dashboard(
            Long viewerUserId,
            String viewerRole,
            LocalDate date,
            Long farmId
    );

    Optional<AttendanceResponse> findAttendance(Long userId, Long farmId, LocalDate date);

    Optional<AttendanceResponse> findAttendanceById(Long attendanceId);

    AttendanceResponse insertCheckIn(
            Long userId,
            Long farmId,
            LocalDateTime checkInTime,
            String status,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters
    );

    AttendanceResponse updateCheckOut(
            Long attendanceId,
            LocalDateTime checkOutTime,
            String location,
            Double latitude,
            Double longitude,
            Double accuracyMeters
    );

    void createQrToken(
            String token,
            String manualCode,
            Long farmId,
            Long createdBy,
            LocalDateTime expiresAt
    );

    boolean activeManualCodeExists(String manualCode, LocalDateTime now);

    Optional<AttendanceQrSession> findValidQrSessionByToken(String token, LocalDateTime now);

    Optional<AttendanceQrSession> findValidQrSessionByManualCode(String manualCode, LocalDateTime now);

    boolean isWorkerAssignedToFarm(Long userId, Long farmId);

    Optional<String> findFarmBoundary(Long farmId);

    List<AttendanceResponse> findReport(
            Long viewerUserId,
            String viewerRole,
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    );

    List<AttendanceReportSummary> findReportSummary(
            Long viewerUserId,
            String viewerRole,
            LocalDate from,
            LocalDate to,
            Long farmId,
            Long userId,
            String search
    );
}
