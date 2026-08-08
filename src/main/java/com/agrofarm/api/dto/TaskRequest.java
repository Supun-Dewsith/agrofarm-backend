package com.agrofarm.api.dto;

import com.agrofarm.api.util.TaskPriority;
import com.agrofarm.api.util.TaskStatus;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {


    @NotBlank(message = "Title is required")
    private String title;


    private String description;


    @NotNull(message = "Priority is required")
    private TaskPriority priority;


    @NotNull(message = "Status is required")
    private TaskStatus status;


    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;


    @NotNull(message = "Field ID is required")
    private Long fieldId;


    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

}