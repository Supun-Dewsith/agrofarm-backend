package com.agrofarm.api.dto.task;

import com.agrofarm.api.util.TaskPriority;

import java.time.LocalDate;
import java.util.List;

public record TaskRequest(

        String title,

        String description,

        String farmName,

        String fieldName,

        String assigneeName,

        String supervisorName,

        List<String> workerNames,

        LocalDate dueDate,

        TaskPriority priority
) {
}
