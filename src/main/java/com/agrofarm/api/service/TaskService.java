package com.agrofarm.api.service;

import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;
import com.agrofarm.api.dto.task.TaskDashboardResponse;

import java.util.List;

public interface TaskService {

    /**
     * Create a new task
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Get all tasks
     * If statusFilter is null or empty, return all tasks.
     */
    List<TaskResponse> getAllTasks(String statusFilter);

    /**
     * Get task by id
     */
    TaskResponse getTaskById(Long id);

    /**
     * Update task
     */
    TaskResponse updateTask(Long id, TaskRequest request);

    /**
     * Delete task
     */
    void deleteTask(Long id);

    /**
     * Dashboard Summary
     */
    TaskDashboardResponse getTaskDashboardSummary();

}