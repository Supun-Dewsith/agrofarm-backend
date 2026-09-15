package com.agrofarm.api.dto.task;

import com.agrofarm.api.util.TaskPriority;
import com.agrofarm.api.util.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public record TaskRequest(
        String title,
        String description,
        Long farmId,
        Long fieldId,
        Long supervisorId,
        List<Long> workerIds,
        LocalDate dueDate,
        TaskPriority priority,
        TaskStatus status
) {
}
