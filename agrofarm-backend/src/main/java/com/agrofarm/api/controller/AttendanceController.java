package com.agrofarm.api.controller;

import com.agrofarm.api.dto.AttendanceCodeRequest;
import com.agrofarm.api.dto.QrAttendanceRequest;
import com.agrofarm.api.dto.QrAttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceDashboardResponse;
import com.agrofarm.api.dto.attendance.AttendanceFarmResponse;
import com.agrofarm.api.dto.attendance.AttendanceReportSummary;
import com.agrofarm.api.dto.attendance.AttendanceResponse;
import com.agrofarm.api.dto.attendance.AttendanceWorkerResponse;
import com.agrofarm.api.dto.attendance.QrGenerateResponse;
import com.agrofarm.api.dto.attendance.QrLiveCodeResponse;
import com.agrofarm.api.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/farms")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceFarmResponse>> farms() {
        return ResponseEntity.ok(attendanceService.getFarms());
    }

    @GetMapping("/workers")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceWorkerResponse>> workers(
            @RequestParam(required = false) Long farmId,
            @RequestParam(defaultValue = "") String search
    ) {
        return ResponseEntity.ok(attendanceService.getWorkers(farmId, search));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceResponse>> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long farmId,
            @RequestParam(defaultValue = "") String search
    ) {
        return ResponseEntity.ok(attendanceService.getDailyAttendance(date, farmId, search));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<AttendanceDashboardResponse> dashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long farmId
    ) {
        return ResponseEntity.ok(
                attendanceService.getDashboard(
                        date != null ? date : LocalDate.now(),
                        farmId
                )
        );
    }

    @PostMapping("/qr/generate")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR')")
    public ResponseEntity<QrGenerateResponse> generateQr(@RequestParam Long farmId) {
        return ResponseEntity.ok(attendanceService.generateQr(farmId));
    }

    @GetMapping("/qr/live-code")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR')")
    public ResponseEntity<QrLiveCodeResponse> qrLiveCode(@RequestParam String token) {
        return ResponseEntity.ok(attendanceService.getQrLiveCode(token));
    }

    @PostMapping("/qr-scan")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<QrAttendanceResponse> qrScan(
            @Valid @RequestBody QrAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceService.processQrAttendance(request));
    }

    @PostMapping("/code-check")
    @PreAuthorize("hasAuthority('FARMER')")
    public ResponseEntity<QrAttendanceResponse> codeCheck(
            @Valid @RequestBody AttendanceCodeRequest request
    ) {
        return ResponseEntity.ok(attendanceService.processCodeAttendance(request));
    }

    @PutMapping("/check-out/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR')")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long userId,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String location = body != null && body.get("location") != null
                ? body.get("location").toString()
                : null;

        Double latitude = number(body, "latitude");
        Double longitude = number(body, "longitude");
        Double accuracyMeters = number(body, "accuracyMeters");

        return ResponseEntity.ok(
                attendanceService.checkOut(
                        userId,
                        location,
                        latitude,
                        longitude,
                        accuracyMeters
                )
        );
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceResponse>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(attendanceService.getHistory(userId));
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceResponse>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "") String search
    ) {
        return ResponseEntity.ok(
                attendanceService.getReport(from, to, farmId, userId, search)
        );
    }

    @GetMapping("/report/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
    public ResponseEntity<List<AttendanceReportSummary>> reportSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "") String search
    ) {
        return ResponseEntity.ok(
                attendanceService.getReportSummary(from, to, farmId, userId, search)
        );
    }

    private Double number(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return null;
        }

        Object value = body.get(key);
        return value instanceof Number number
                ? number.doubleValue()
                : Double.valueOf(value.toString());
    }
}
