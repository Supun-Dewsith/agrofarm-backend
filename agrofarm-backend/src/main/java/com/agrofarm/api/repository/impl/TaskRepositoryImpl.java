package com.agrofarm.api.repository.impl;

import com.agrofarm.api.entity.Task;
import com.agrofarm.api.repository.TaskRepository;
import com.agrofarm.api.util.TaskPriority;
import com.agrofarm.api.util.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // CREATE TASK
    // =========================================================

    @Override
    public Task save(Task task) {

        String sql = """
                INSERT INTO tasks (
                    title,
                    description,
                    priority,
                    status,
                    farm_id,
                    assignee_id,
                    supervisor_id,
                    field_id,
                    due_date
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getPriority().name());
            ps.setString(4, task.getStatus().name());
            ps.setLong(5, task.getFarmId());
            ps.setLong(6, task.getAssigneeId());

            if (task.getSupervisorId() != null) {
                ps.setLong(7, task.getSupervisorId());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }

            if (task.getFieldId() != null) {
                ps.setLong(8, task.getFieldId());
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }

            ps.setDate(9, Date.valueOf(task.getDueDate()));

            return ps;

        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new RuntimeException("Failed to create task");
        }

        task.setId(key.longValue());

        return task;
    }

    // =========================================================
    // GET ALL TASKS
    // =========================================================

    @Override
    public List<Task> findAll() {

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    priority,
                    status,
                    farm_id,
                    assignee_id,
                    supervisor_id,
                    field_id,
                    due_date,
                    created_at
                FROM tasks
                ORDER BY created_at DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Task task = mapTask(rs);

                    task.setWorkerIds(
                            findWorkerIds(task.getId())
                    );

                    return task;
                }
        );
    }

    // =========================================================
    // GET TASK BY ID
    // =========================================================

    @Override
    public Optional<Task> findById(Long id) {

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    priority,
                    status,
                    farm_id,
                    assignee_id,
                    supervisor_id,
                    field_id,
                    due_date,
                    created_at
                FROM tasks
                WHERE id = ?
                """;

        List<Task> tasks = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Task task = mapTask(rs);

                    task.setWorkerIds(
                            findWorkerIds(task.getId())
                    );

                    return task;
                },
                id
        );

        return tasks.stream().findFirst();
    }

    // =========================================================
    // UPDATE TASK
    // =========================================================

    @Override
    public Task update(Long id, Task task) {

        String sql = """
                UPDATE tasks
                SET
                    title = ?,
                    description = ?,
                    priority = ?,
                    farm_id = ?,
                    assignee_id = ?,
                    supervisor_id = ?,
                    field_id = ?,
                    due_date = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                task.getTitle(),
                task.getDescription(),
                task.getPriority().name(),
                task.getFarmId(),
                task.getAssigneeId(),
                task.getSupervisorId(),
                task.getFieldId(),
                Date.valueOf(task.getDueDate()),
                id
        );

        task.setId(id);

        return task;
    }

    // =========================================================
    // DELETE TASK
    // =========================================================

    @Override
    public void delete(Long id) {

        jdbcTemplate.update(
                "DELETE FROM tasks WHERE id = ?",
                id
        );
    }

    // =========================================================
    // DELETE TASK ASSIGNMENTS
    // =========================================================

    @Override
    public void deleteAssignments(Long taskId) {

        jdbcTemplate.update(
                "DELETE FROM task_assignments WHERE task_id = ?",
                taskId
        );
    }

    // =========================================================
    // ADD WORKER TO TASK
    // =========================================================

    @Override
    public void addWorker(Long taskId, Long workerId) {

        jdbcTemplate.update(
                """
                INSERT INTO task_assignments (
                    task_id,
                    worker_id
                )
                VALUES (?, ?)
                """,
                taskId,
                workerId
        );
    }

    // =========================================================
    // GET WORKER IDS
    // =========================================================

    @Override
    public List<Long> findWorkerIds(Long taskId) {

        return jdbcTemplate.query(
                """
                SELECT worker_id
                FROM task_assignments
                WHERE task_id = ?
                ORDER BY id
                """,
                (rs, rowNum) -> rs.getLong("worker_id"),
                taskId
        );
    }

    // =========================================================
    // FIND FARM ID BY FARM NAME
    // =========================================================

    @Override
    public Long findFarmIdByName(String farmName) {

        List<Long> ids = jdbcTemplate.query(
                """
                SELECT id
                FROM farms
                WHERE farm_name = ?
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getLong("id"),
                farmName
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // =========================================================
    // FIND FIELD ID BY FIELD NAME + FARM ID
    // =========================================================

    @Override
    public Long findFieldIdByName(String fieldName, Long farmId) {

        List<Long> ids = jdbcTemplate.query(
                """
                SELECT id
                FROM fields
                WHERE field_name = ?
                  AND farm_id = ?
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getLong("id"),
                fieldName,
                farmId
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // =========================================================
    // FIND USER ID BY USER NAME
    // =========================================================

    @Override
    public Long findUserIdByName(String userName) {

        List<Long> ids = jdbcTemplate.query(
                """
                SELECT id
                FROM users
                WHERE user_name = ?
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getLong("id"),
                userName
        );

        return ids.isEmpty() ? null : ids.get(0);
    }

    // =========================================================
    // FIND USER IDS BY USER NAMES
    // =========================================================

    @Override
    public List<Long> findUserIdsByNames(List<String> names) {

        if (names == null || names.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> result = new ArrayList<>();

        for (String name : names) {

            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            Long id = findUserIdByName(name.trim());

            if (id == null) {
                throw new RuntimeException(
                        "Worker not found: " + name
                );
            }

            if (!result.contains(id)) {
                result.add(id);
            }
        }

        return result;
    }

    // =========================================================
    // FIND FARM NAME BY FARM ID
    // =========================================================

    @Override
    public String findFarmNameById(Long farmId) {

        if (farmId == null) {
            return null;
        }

        List<String> names = jdbcTemplate.query(
                """
                SELECT farm_name
                FROM farms
                WHERE id = ?
                """,
                (rs, rowNum) -> rs.getString("farm_name"),
                farmId
        );

        return names.isEmpty() ? null : names.get(0);
    }

    // =========================================================
    // FIND FIELD NAME BY FIELD ID
    // =========================================================

    @Override
    public String findFieldNameById(Long fieldId) {

        if (fieldId == null) {
            return null;
        }

        List<String> names = jdbcTemplate.query(
                """
                SELECT field_name
                FROM fields
                WHERE id = ?
                """,
                (rs, rowNum) -> rs.getString("field_name"),
                fieldId
        );

        return names.isEmpty() ? null : names.get(0);
    }

    // =========================================================
    // FIND USER NAME BY USER ID
    // =========================================================

    @Override
    public String findUserNameById(Long userId) {

        if (userId == null) {
            return null;
        }

        List<String> names = jdbcTemplate.query(
                """
                SELECT user_name
                FROM users
                WHERE id = ?
                """,
                (rs, rowNum) -> rs.getString("user_name"),
                userId
        );

        return names.isEmpty() ? null : names.get(0);
    }

    // =========================================================
    // FIND WORKER NAMES BY TASK ID
    // =========================================================

    @Override
    public List<String> findWorkerNames(Long taskId) {

        return jdbcTemplate.query(
                """
                SELECT u.user_name
                FROM task_assignments ta
                INNER JOIN users u
                    ON u.id = ta.worker_id
                WHERE ta.task_id = ?
                ORDER BY ta.id
                """,
                (rs, rowNum) -> rs.getString("user_name"),
                taskId
        );
    }

    // =========================================================
    // UPDATE TASK STATUS
    // =========================================================

    @Override
    public void updateStatus(Long id, TaskStatus status) {

        jdbcTemplate.update(
                """
                UPDATE tasks
                SET status = ?
                WHERE id = ?
                """,
                status.name(),
                id
        );
    }

    // =========================================================
    // MAP RESULT SET TO TASK
    // =========================================================

    private Task mapTask(ResultSet rs) throws java.sql.SQLException {

        Task task = new Task();

        task.setId(
                rs.getLong("id")
        );

        task.setTitle(
                rs.getString("title")
        );

        task.setDescription(
                rs.getString("description")
        );

        String priority = rs.getString("priority");

        if (priority != null) {
            task.setPriority(
                    TaskPriority.valueOf(priority)
            );
        }

        String status = rs.getString("status");

        if (status != null) {
            task.setStatus(
                    TaskStatus.valueOf(status)
            );
        }

        task.setFarmId(
                rs.getLong("farm_id")
        );

        task.setAssigneeId(
                rs.getLong("assignee_id")
        );

        long supervisorId = rs.getLong("supervisor_id");

        if (!rs.wasNull()) {
            task.setSupervisorId(supervisorId);
        }

        long fieldId = rs.getLong("field_id");

        if (!rs.wasNull()) {
            task.setFieldId(fieldId);
        }

        Date dueDate = rs.getDate("due_date");

        if (dueDate != null) {
            task.setDueDate(
                    dueDate.toLocalDate()
            );
        }

        if (rs.getTimestamp("created_at") != null) {
            task.setCreatedAt(
                    rs.getTimestamp("created_at")
                            .toLocalDateTime()
            );
        }

        return task;
    }
}