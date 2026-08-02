package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;
import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.entity.Task;
import com.agrofarm.api.repository.TaskRepository;
import com.agrofarm.api.service.TaskService;
import com.agrofarm.api.util.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    /**
     * Create Task
     */
    @Override
    public TaskResponse createTask(TaskRequest request) {

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .assigneeId(request.getAssigneeId())
                .dueDate(request.getDueDate())
                .locationTag(request.getLocationTag())
                .build();

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    /**
     * Entity -> DTO Mapping
     */
    private TaskResponse mapToResponse(Task task) {

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .assigneeId(task.getAssigneeId())
                .dueDate(task.getDueDate())
                .locationTag(task.getLocationTag())
                .build();
    }
    /**
     * Get All Tasks
     */
    @Override
    public List<TaskResponse> getAllTasks(String statusFilter) {

        List<Task> tasks;

        if (statusFilter == null || statusFilter.isBlank()) {

            tasks = taskRepository.findAll();

        } else {

            TaskStatus status = TaskStatus.valueOf(statusFilter.toUpperCase());

            tasks = taskRepository.findByStatus(status);
        }

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get Task By ID
     */
    @Override
    public TaskResponse getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id : " + id));

        return mapToResponse(task);
    }
    /**
     * Update Task
     */
    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id : " + id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setAssigneeId(request.getAssigneeId());
        task.setDueDate(request.getDueDate());
        task.setLocationTag(request.getLocationTag());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    /**
     * Delete Task
     */
    @Override
    public void deleteTask(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with id : " + id));

        taskRepository.delete(task);
    }

    /**
     * Dashboard Summary
     */
    @Override
    public TaskDashboardResponse getTaskDashboardSummary() {

        long totalTasks = taskRepository.count();

        long pendingCount = taskRepository.countByStatus(TaskStatus.PENDING);

        long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);

        long completedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);

        List<TaskResponse> tasks = taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new TaskDashboardResponse(
                totalTasks,
                pendingCount,
                inProgressCount,
                completedCount,
                tasks
        );
    }

}