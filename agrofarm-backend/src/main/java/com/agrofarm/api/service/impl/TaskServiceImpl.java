package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskRequest;
import com.agrofarm.api.entity.Task;
import com.agrofarm.api.repository.TaskRepository;
import com.agrofarm.api.service.TaskService;
import com.agrofarm.api.util.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public TaskDashboardResponse createTask(TaskRequest request) {

        validateRequest(request);

        Long farmId =
                taskRepository.findFarmIdByName(request.farmName());

        if (farmId == null) {
            throw new RuntimeException(
                    "Farm not found: " + request.farmName()
            );
        }

        Long fieldId = null;

        if (request.fieldName() != null &&
                !request.fieldName().isBlank()) {

            fieldId = taskRepository.findFieldIdByName(
                    request.fieldName(),
                    farmId
            );

            if (fieldId == null) {
                throw new RuntimeException(
                        "Field not found: " + request.fieldName()
                );
            }
        }

        Long assigneeId =
                taskRepository.findUserIdByName(request.assigneeName());

        if (assigneeId == null) {
            throw new RuntimeException(
                    "Assignee not found: " + request.assigneeName()
            );
        }

        Long supervisorId = null;

        if (request.supervisorName() != null &&
                !request.supervisorName().isBlank()) {

            supervisorId =
                    taskRepository.findUserIdByName(
                            request.supervisorName()
                    );

            if (supervisorId == null) {
                throw new RuntimeException(
                        "Supervisor not found: " +
                                request.supervisorName()
                );
            }
        }

        List<Long> workerIds =
                taskRepository.findUserIdsByNames(
                        request.workerNames()
                );

        Task task = new Task();

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setStatus(TaskStatus.PENDING);

        task.setFarmId(farmId);
        task.setFieldId(fieldId);
        task.setAssigneeId(assigneeId);
        task.setSupervisorId(supervisorId);

        task.setDueDate(request.dueDate());
        task.setWorkerIds(workerIds);

        Task savedTask = taskRepository.save(task);

        saveWorkers(savedTask.getId(), workerIds);

        return toResponse(savedTask);
    }

    @Override
    public List<TaskDashboardResponse> getAllTasks() {

        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TaskDashboardResponse getTaskById(Long id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with id: " + id
                        )
                );

        return toResponse(task);
    }

    @Override
    @Transactional
    public TaskDashboardResponse updateTask(
            Long id,
            TaskRequest request
    ) {

        validateRequest(request);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with id: " + id
                        )
                );

        Long farmId =
                taskRepository.findFarmIdByName(request.farmName());

        if (farmId == null) {
            throw new RuntimeException(
                    "Farm not found: " + request.farmName()
            );
        }

        Long fieldId = null;

        if (request.fieldName() != null &&
                !request.fieldName().isBlank()) {

            fieldId = taskRepository.findFieldIdByName(
                    request.fieldName(),
                    farmId
            );

            if (fieldId == null) {
                throw new RuntimeException(
                        "Field not found: " + request.fieldName()
                );
            }
        }

        Long assigneeId =
                taskRepository.findUserIdByName(
                        request.assigneeName()
                );

        if (assigneeId == null) {
            throw new RuntimeException(
                    "Assignee not found: " +
                            request.assigneeName()
            );
        }

        Long supervisorId = null;

        if (request.supervisorName() != null &&
                !request.supervisorName().isBlank()) {

            supervisorId =
                    taskRepository.findUserIdByName(
                            request.supervisorName()
                    );

            if (supervisorId == null) {
                throw new RuntimeException(
                        "Supervisor not found: " +
                                request.supervisorName()
                );
            }
        }

        List<Long> workerIds =
                taskRepository.findUserIdsByNames(
                        request.workerNames()
                );

        existingTask.setTitle(request.title());
        existingTask.setDescription(request.description());
        existingTask.setPriority(request.priority());

        existingTask.setFarmId(farmId);
        existingTask.setFieldId(fieldId);
        existingTask.setAssigneeId(assigneeId);
        existingTask.setSupervisorId(supervisorId);

        existingTask.setDueDate(request.dueDate());
        existingTask.setWorkerIds(workerIds);

        Task updatedTask =
                taskRepository.update(id, existingTask);

        /*
         * Replace existing workers with new worker list.
         */
        taskRepository.deleteAssignments(id);

        saveWorkers(id, workerIds);

        return toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {

        taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with id: " + id
                        )
                );

        /*
         * task_assignments also has ON DELETE CASCADE,
         * so deleting the task automatically removes
         * worker assignments.
         */
        taskRepository.delete(id);
    }

    @Override
    public TaskDashboardResponse updateStatus(
            Long id,
            String status
    ) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with id: " + id
                        )
                );

        TaskStatus newStatus;

        try {
            newStatus = TaskStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Invalid task status: " + status
            );
        }

        task.setStatus(newStatus);

        /*
         * Direct status update.
         */
        taskRepository.updateStatus(id, newStatus);

        return toResponse(task);
    }

    private void saveWorkers(
            Long taskId,
            List<Long> workerIds
    ) {

        if (workerIds == null || workerIds.isEmpty()) {
            return;
        }

        for (Long workerId : workerIds) {
            taskRepository.addWorker(
                    taskId,
                    workerId
            );
        }
    }

    private TaskDashboardResponse toResponse(Task task) {

        String farmName =
                taskRepository.findFarmNameById(
                        task.getFarmId()
                );

        String fieldName =
                taskRepository.findFieldNameById(
                        task.getFieldId()
                );

        String assigneeName =
                taskRepository.findUserNameById(
                        task.getAssigneeId()
                );

        String supervisorName =
                taskRepository.findUserNameById(
                        task.getSupervisorId()
                );

        List<String> workerNames =
                taskRepository.findWorkerNames(
                        task.getId()
                );

        return new TaskDashboardResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                farmName,
                fieldName,
                assigneeName,
                supervisorName,
                workerNames,
                task.getDueDate(),
                task.getPriority(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }

    private void validateRequest(TaskRequest request) {

        if (request == null) {
            throw new RuntimeException(
                    "Task request cannot be null"
            );
        }

        if (request.title() == null ||
                request.title().isBlank()) {

            throw new RuntimeException(
                    "Task title is required"
            );
        }

        if (request.farmName() == null ||
                request.farmName().isBlank()) {

            throw new RuntimeException(
                    "Farm name is required"
            );
        }

        if (request.assigneeName() == null ||
                request.assigneeName().isBlank()) {

            throw new RuntimeException(
                    "Assignee name is required"
            );
        }

        if (request.dueDate() == null) {

            throw new RuntimeException(
                    "Due date is required"
            );
        }

        if (request.priority() == null) {

            throw new RuntimeException(
                    "Priority is required"
            );
        }
    }
}