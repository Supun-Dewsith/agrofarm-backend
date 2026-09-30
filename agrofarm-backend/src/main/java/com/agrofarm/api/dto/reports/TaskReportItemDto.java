package com.agrofarm.api.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskReportItemDto {
    private Long id;
    private String title;
    private String description;
    private Long farmId;
    private String farmName;
    private Long fieldId;
    private String fieldName;
    private Long assigneeId;
    private String assigneeName;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
}
