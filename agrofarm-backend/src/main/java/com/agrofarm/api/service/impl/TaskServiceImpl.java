package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskOptionsResponse;
import com.agrofarm.api.dto.task.TaskOptionsResponse.Option;
import com.agrofarm.api.dto.task.TaskRequest;

import com.agrofarm.api.entity.Task;
import com.agrofarm.api.entity.UserEntity;

import com.agrofarm.api.repository.TaskRepository;
import com.agrofarm.api.repository.UserRepository;

import com.agrofarm.api.service.TaskService;
import com.agrofarm.api.service.NotificationService;

import com.agrofarm.api.util.Role;
import com.agrofarm.api.util.TaskStatus;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            UserRepository userRepository,
            JdbcTemplate jdbcTemplate,
            NotificationService notificationService
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
    }

    /* =====================================================
       ERROR HELPERS
    ===================================================== */

    private ResponseStatusException badRequest(
            String message
    ) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private void denyAccess() {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have access to this task or farm"
        );
    }

    /* =====================================================
       AUTHENTICATED USER
    ===================================================== */

    private UserEntity currentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in"
            );
        }

        UserEntity user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            denyAccess();
        }

        return user;
    }

    /* =====================================================
       DATABASE HELPERS
    ===================================================== */

    private boolean exists(
            String sql,
            Object... arguments
    ) {
        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                arguments
        );

        return count != null && count > 0;
    }

    private Task findTask(Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Task not found"
                        )
                );
    }

    /* =====================================================
       ACCESS RULES
    ===================================================== */

    private void requireManager(UserEntity user) {

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.SUPERVISOR) {

            denyAccess();
        }
    }

    private boolean canManageFarm(
            UserEntity user,
            Long farmId
    ) {

        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() != Role.SUPERVISOR) {
            return false;
        }

        return exists(
                """
                SELECT COUNT(*)
                FROM farm_assignments
                WHERE user_id = ?
                  AND farm_id = ?
                """,
                user.getId(),
                farmId
        );
    }

    private boolean canViewTask(
            UserEntity user,
            Task task
    ) {

        // Admin: all tasks.
        // Supervisor: all tasks within assigned farms.
        if (canManageFarm(user, task.getFarmId())) {
            return true;
        }

        if (user.getRole() != Role.FARMER) {
            return false;
        }

        // Support older tasks using assignee_id.
        if (Objects.equals(
                user.getId(),
                task.getAssigneeId()
        )) {
            return true;
        }

        // Support tasks assigned to multiple farmers.
        return taskRepository
                .findWorkerIds(task.getId())
                .contains(user.getId());
    }

    private Task requireReadableTask(
            Long id,
            UserEntity user
    ) {

        Task task = findTask(id);

        if (!canViewTask(user, task)) {
            denyAccess();
        }

        return task;
    }

    /* =====================================================
       GET TASKS
    ===================================================== */

    @Override
    public List<TaskDashboardResponse> getAllTasks() {

        UserEntity user = currentUser();

        return taskRepository.findAll()
                .stream()
                .filter(task -> canViewTask(user, task))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TaskDashboardResponse getTaskById(Long id) {

        UserEntity user = currentUser();

        Task task = requireReadableTask(id, user);

        return toResponse(task);
    }

    /* =====================================================
       DROPDOWN OPTIONS
    ===================================================== */

    private List<Option> queryOptions(
            String sql,
            Object... arguments
    ) {

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new Option(
                        resultSet.getLong("id"),
                        resultSet.getString("name")
                ),
                arguments
        );
    }

    @Override
    public TaskOptionsResponse getOptions(Long farmId) {

        UserEntity user = currentUser();

        requireManager(user);

        List<Option> farms;

        if (user.getRole() == Role.ADMIN) {

            farms = queryOptions(
                    """
                    SELECT
                        id,
                        farm_name AS name
                    FROM farms
                    ORDER BY farm_name, id
                    """
            );

        } else {

            farms = queryOptions(
                    """
                    SELECT
                        f.id,
                        f.farm_name AS name
                    FROM farms f
                    INNER JOIN farm_assignments fa
                        ON fa.farm_id = f.id
                    WHERE fa.user_id = ?
                    ORDER BY f.farm_name, f.id
                    """,
                    user.getId()
            );
        }

        // Initial modal request: load available farms.
        if (farmId == null) {

            return new TaskOptionsResponse(
                    farms,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        if (!canManageFarm(user, farmId)) {
            denyAccess();
        }

        if (!exists(
                "SELECT COUNT(*) FROM farms WHERE id = ?",
                farmId
        )) {
            throw badRequest("Farm not found");
        }

        List<Option> fields = queryOptions(
                """
                SELECT
                    id,
                    field_name AS name
                FROM fields
                WHERE farm_id = ?
                ORDER BY field_name, id
                """,
                farmId
        );

        String usersSql = """
                SELECT
                    u.id,
                    u.user_name AS name
                FROM users u
                INNER JOIN farm_assignments fa
                    ON fa.user_id = u.id
                WHERE fa.farm_id = ?
                  AND u.role = ?
                  AND u.enabled = 1
                ORDER BY u.user_name
                """;

        List<Option> supervisors = queryOptions(
                usersSql,
                farmId,
                "SUPERVISOR"
        );

        List<Option> workers = queryOptions(
                usersSql,
                farmId,
                "FARMER"
        );

        return new TaskOptionsResponse(
                farms,
                fields,
                supervisors,
                workers
        );
    }

    /* =====================================================
       VALIDATION
    ===================================================== */

    private void validateFarmMember(
            Long userId,
            Long farmId,
            String requiredRole
    ) {

        if (userId == null) {
            throw badRequest(
                    "Select a " +
                            requiredRole.toLowerCase()
            );
        }

        boolean valid = exists(
                """
                SELECT COUNT(*)
                FROM users u
                INNER JOIN farm_assignments fa
                    ON fa.user_id = u.id
                WHERE u.id = ?
                  AND fa.farm_id = ?
                  AND u.role = ?
                  AND u.enabled = 1
                """,
                userId,
                farmId,
                requiredRole
        );

        if (!valid) {

            throw badRequest(
                    "Select an active " +
                            requiredRole.toLowerCase() +
                            " assigned to this farm"
            );
        }
    }

    private List<Long> validateRequest(
            TaskRequest request,
            UserEntity user
    ) {

        requireManager(user);

        if (request == null) {
            throw badRequest("Task request is required");
        }

        if (request.title() == null ||
                request.title().isBlank() ||
                request.title().trim().length() > 255) {

            throw badRequest(
                    "Enter a title of 1–255 characters"
            );
        }

        if (request.description() != null &&
                request.description().length() > 1000) {

            throw badRequest(
                    "Description must be at most 1000 characters"
            );
        }

        if (request.farmId() == null) {
            throw badRequest("Select a farm");
        }

        boolean farmExists = exists(
                "SELECT COUNT(*) FROM farms WHERE id = ?",
                request.farmId()
        );

        if (!farmExists) {
            throw badRequest("Select a valid farm");
        }

        if (!canManageFarm(user, request.farmId())) {
            denyAccess();
        }

        // Field is optional.
        if (request.fieldId() != null) {

            boolean validField = exists(
                    """
                    SELECT COUNT(*)
                    FROM fields
                    WHERE id = ?
                      AND farm_id = ?
                    """,
                    request.fieldId(),
                    request.farmId()
            );

            if (!validField) {

                throw badRequest(
                        "The field must belong to the selected farm"
                );
            }
        }

        if (request.priority() == null) {
            throw badRequest("Priority is required");
        }

        if (request.status() == null) {
            throw badRequest("Status is required");
        }

        if (request.dueDate() == null) {
            throw badRequest("Due date is required");
        }

        validateFarmMember(
                request.supervisorId(),
                request.farmId(),
                "SUPERVISOR"
        );

        if (request.workerIds() == null ||
                request.workerIds().isEmpty()) {

            throw badRequest(
                    "Select at least one farmer"
            );
        }

        List<Long> workerIds = request.workerIds()
                .stream()
                .distinct()
                .toList();

        for (Long workerId : workerIds) {

            validateFarmMember(
                    workerId,
                    request.farmId(),
                    "FARMER"
            );
        }

        return workerIds;
    }

    /* =====================================================
       MAP REQUEST TO ENTITY
    ===================================================== */

    private void applyRequest(
            Task task,
            TaskRequest request,
            List<Long> workerIds
    ) {

        task.setTitle(request.title().trim());

        task.setDescription(
                request.description() == null
                        ? null
                        : request.description().trim()
        );

        task.setFarmId(request.farmId());
        task.setFieldId(request.fieldId());

        task.setSupervisorId(
                request.supervisorId()
        );

        /*
         * Existing schema requires assignee_id.
         * Store the first selected farmer here.
         * task_assignments stores ALL selected farmers.
         */
        task.setAssigneeId(workerIds.get(0));
        task.setWorkerIds(workerIds);

        task.setPriority(request.priority());
        task.setStatus(request.status());
        task.setDueDate(request.dueDate());
    }

    /* =====================================================
       CREATE
    ===================================================== */

    @Override
    @Transactional
    public TaskDashboardResponse createTask(
            TaskRequest request
    ) {

        UserEntity user = currentUser();

        List<Long> workerIds = validateRequest(
                request,
                user
        );

        Task task = new Task();

        applyRequest(task, request, workerIds);

        Task savedTask = taskRepository.save(task);

        for (Long workerId : workerIds) {

            taskRepository.addWorker(
                    savedTask.getId(),
                    workerId
            );
        }

        notificationService.notifyTaskAssigned(
                savedTask.getId(), savedTask.getTitle(),
                savedTask.getSupervisorId(), workerIds
        );

        return toResponse(
                findTask(savedTask.getId())
        );
    }

    /* =====================================================
       UPDATE
    ===================================================== */

    @Override
    @Transactional
    public TaskDashboardResponse updateTask(
            Long id,
            TaskRequest request
    ) {

        UserEntity user = currentUser();

        requireManager(user);

        // Verify access to the existing task's farm.
        Task task = requireReadableTask(id, user);

        // Also verify access to the new selected farm.
        List<Long> workerIds = validateRequest(
                request,
                user
        );

        applyRequest(task, request, workerIds);

        taskRepository.update(id, task);

        /*
         * Your existing repository update method does not
         * update status, so update it explicitly.
         */
        taskRepository.updateStatus(
                id,
                request.status()
        );

        taskRepository.deleteAssignments(id);

        for (Long workerId : workerIds) {

            taskRepository.addWorker(
                    id,
                    workerId
            );
        }

        return toResponse(findTask(id));
    }

    /* =====================================================
       UPDATE STATUS
    ===================================================== */

    @Override
    @Transactional
    public TaskDashboardResponse updateStatus(
            Long id,
            String status
    ) {

        UserEntity user = currentUser();

        requireManager(user);

        Task task = requireReadableTask(id, user);

        TaskStatus newStatus;

        try {

            newStatus = TaskStatus.valueOf(status);

        } catch (
                IllegalArgumentException |
                NullPointerException exception
        ) {

            throw badRequest(
                    "Invalid task status"
            );
        }

        taskRepository.updateStatus(
                id,
                newStatus
        );

        task.setStatus(newStatus);

        return toResponse(task);
    }

    /* =====================================================
       DELETE
    ===================================================== */

    @Override
    @Transactional
    public void deleteTask(Long id) {

        UserEntity user = currentUser();

        requireManager(user);

        requireReadableTask(id, user);

        /*
         * task_assignments rows are deleted by the existing
         * ON DELETE CASCADE foreign key.
         */
        taskRepository.delete(id);
    }

    /* =====================================================
       RESPONSE
    ===================================================== */

    private TaskDashboardResponse toResponse(
            Task task
    ) {

        List<Long> workerIds =
                taskRepository.findWorkerIds(
                        task.getId()
                );

        // Support historical assignee-only tasks.
        if (workerIds.isEmpty() &&
                task.getAssigneeId() != null) {

            workerIds = List.of(
                    task.getAssigneeId()
            );
        }

        List<String> workerNames = workerIds
                .stream()
                .map(taskRepository::findUserNameById)
                .filter(Objects::nonNull)
                .toList();

        return new TaskDashboardResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),

                taskRepository.findFarmNameById(
                        task.getFarmId()
                ),

                taskRepository.findFieldNameById(
                        task.getFieldId()
                ),

                taskRepository.findUserNameById(
                        task.getAssigneeId()
                ),

                taskRepository.findUserNameById(
                        task.getSupervisorId()
                ),

                workerNames,

                task.getDueDate(),
                task.getPriority(),
                task.getStatus(),
                task.getCreatedAt(),

                task.getFarmId(),
                task.getFieldId(),
                task.getSupervisorId(),
                workerIds
        );
    }
}
