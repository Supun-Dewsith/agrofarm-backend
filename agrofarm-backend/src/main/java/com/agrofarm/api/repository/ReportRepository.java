package com.agrofarm.api.repository;

import com.agrofarm.api.dto.reports.*;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository {
    List<FinancialTransactionDto> findFinancialTransactions(Long farmId, LocalDate from, LocalDate to);
    List<CropReportItemDto> findCropReports(Long farmId);
    List<AttendanceReportItemDto> findAttendanceReports(Long farmId, LocalDate from, LocalDate to);
    List<TaskReportItemDto> findTaskReports(Long farmId, LocalDate from, LocalDate to);
    List<DiseaseReportItemDto> findDiseaseReports(int limit);
    FinancialTransactionDto insertFinancialTransaction(CreateFinancialTransactionRequest request, Long userId);
}
