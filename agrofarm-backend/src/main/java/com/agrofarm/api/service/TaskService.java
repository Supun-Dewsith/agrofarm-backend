package com.agrofarm.api.service;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskOptionsResponse;
import com.agrofarm.api.dto.task.TaskRequest;

import java.util.List;

public interface TaskService {

    TaskOptionsResponse getOptions(Long farmId);

    TaskDashboardResponse createTask(
            TaskRequest request
    );

    List<TaskDashboardResponse> getAllTasks();

    TaskDashboardResponse getTaskById(Long id);

    TaskDashboardResponse updateTask(
            Long id,
            TaskRequest request
    );

    TaskDashboardResponse updateStatus(
            Long id,
            String status
    );

    void deleteTask(Long id);
}