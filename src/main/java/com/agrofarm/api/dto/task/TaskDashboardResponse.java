package com.agrofarm.api.dto.task;

import com.agrofarm.api.dto.TaskResponse;

import java.util.List;

public record TaskDashboardResponse(

        long totalTasks,

        long pendingCount,

        long inProgressCount,

        long completedCount,

        List<TaskResponse> tasks

) {
}
