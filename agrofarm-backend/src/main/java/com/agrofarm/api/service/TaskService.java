package com.agrofarm.api.service;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskRequest;

import java.util.List;

public interface TaskService {

    TaskDashboardResponse createTask(TaskRequest request);

    List<TaskDashboardResponse> getAllTasks();

    TaskDashboardResponse getTaskById(Long id);

    TaskDashboardResponse updateTask(Long id, TaskRequest request);

    void deleteTask(Long id);

    TaskDashboardResponse updateStatus(Long id, String status);
}