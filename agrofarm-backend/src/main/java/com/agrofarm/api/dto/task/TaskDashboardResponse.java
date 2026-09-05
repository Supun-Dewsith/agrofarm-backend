package com.agrofarm.api.dto.task;
import com.agrofarm.api.util.TaskPriority;
import com.agrofarm.api.util.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskDashboardResponse(

        Long id,

        String title,

        String description,

        String farmName,

        String fieldName,

        String assigneeName,

        String supervisorName,

        List<String> workerNames,

        LocalDate dueDate,

        TaskPriority priority,

        TaskStatus status,

        LocalDateTime createdAt
) {
}