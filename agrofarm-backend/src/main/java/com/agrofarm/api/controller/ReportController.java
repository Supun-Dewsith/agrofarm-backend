package com.agrofarm.api.controller;

import com.agrofarm.api.dto.reports.*;
import com.agrofarm.api.service.ReportService;
import com.agrofarm.api.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyAuthority('ADMIN','SUPERVISOR','FARMER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/financial")
    public ResponseEntity<FinancialTransactionDto> createFinancialTransaction(
            @Valid @RequestBody CreateFinancialTransactionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = (user != null && user.getId() != null) ? user.getId() : null;
        return ResponseEntity.ok(reportService.createFinancialTransaction(request, userId));
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getSummary(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getSummary(farmId, from, to));
    }

    @GetMapping("/financial")
    public ResponseEntity<List<FinancialTransactionDto>> getFinancialReport(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getFinancialReport(farmId, from, to));
    }

    @GetMapping("/crops")
    public ResponseEntity<List<CropReportItemDto>> getCropReport(
            @RequestParam(required = false) Long farmId
    ) {
        return ResponseEntity.ok(reportService.getCropReport(farmId));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceReportItemDto>> getAttendanceReport(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getAttendanceReport(farmId, from, to));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskReportItemDto>> getTaskReport(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.getTaskReport(farmId, from, to));
    }

    @GetMapping("/disease")
    public ResponseEntity<List<DiseaseReportItemDto>> getDiseaseReport(
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(reportService.getDiseaseReport(limit));
    }
}
