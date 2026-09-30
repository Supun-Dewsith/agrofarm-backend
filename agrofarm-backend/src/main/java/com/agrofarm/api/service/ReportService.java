package com.agrofarm.api.service;

import com.agrofarm.api.dto.reports.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    ReportSummaryDto getSummary(Long farmId, LocalDate from, LocalDate to);
    List<FinancialTransactionDto> getFinancialReport(Long farmId, LocalDate from, LocalDate to);
    List<CropReportItemDto> getCropReport(Long farmId);
    List<AttendanceReportItemDto> getAttendanceReport(Long farmId, LocalDate from, LocalDate to);
    List<TaskReportItemDto> getTaskReport(Long farmId, LocalDate from, LocalDate to);
    List<DiseaseReportItemDto> getDiseaseReport(int limit);
    FinancialTransactionDto createFinancialTransaction(CreateFinancialTransactionRequest request, Long userId);
    ReportAiChatResponse chatWithReportAssistant(ReportAiChatRequest request);
}
