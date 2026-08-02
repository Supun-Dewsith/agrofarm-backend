package com.agrofarm.api.dto;

import com.agrofarm.api.util.TaskPriority;
import com.agrofarm.api.util.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskPriority priority;

    private TaskStatus status;

    private Long assigneeId;

    private LocalDate dueDate;

    private String locationTag;

}
