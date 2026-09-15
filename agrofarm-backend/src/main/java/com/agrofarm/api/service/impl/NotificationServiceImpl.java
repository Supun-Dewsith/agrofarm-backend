package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.notification.NotificationResponse;
import com.agrofarm.api.entity.UserEntity;
import com.agrofarm.api.repository.UserRepository;
import com.agrofarm.api.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    /*
     * Return the currently authenticated user.
     */
    private UserEntity getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(
                        authentication.getPrincipal()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please sign in to continue"
            );
        }

        return userRepository
                .findByUsername(
                        authentication.getName()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Authenticated user not found"
                        )
                );
    }

    /*
     * Get notifications belonging only to
     * the currently logged-in user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMine(
            int limit
    ) {

        int safeLimit = Math.max(
                1,
                Math.min(limit, 100)
        );

        String sql = """
                SELECT
                    id,
                    type,
                    title,
                    message,
                    reference_type,
                    reference_id,
                    is_read,
                    created_at
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {

                    Timestamp createdAt =
                            resultSet.getTimestamp(
                                    "created_at"
                            );

                    return new NotificationResponse(
                            resultSet.getLong("id"),
                            resultSet.getString("type"),
                            resultSet.getString("title"),
                            resultSet.getString("message"),
                            resultSet.getString(
                                    "reference_type"
                            ),
                            resultSet.getObject(
                                    "reference_id",
                                    Long.class
                            ),
                            resultSet.getBoolean(
                                    "is_read"
                            ),
                            createdAt == null
                                    ? null
                                    : createdAt.toLocalDateTime()
                    );
                },
                getCurrentUser().getId(),
                safeLimit
        );
    }

    /*
     * Get unread notification count.
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {

        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM notifications
                WHERE user_id = ?
                  AND is_read = 0
                """,
                Long.class,
                getCurrentUser().getId()
        );

        return count == null ? 0L : count;
    }

    /*
     * Mark one notification as read.
     *
     * user_id is included in the query so that
     * one user cannot update another user's
     * notification.
     */
    @Override
    public void markAsRead(
            Long notificationId
    ) {

        if (notificationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification ID is required"
            );
        }

        jdbcTemplate.update(
                """
                UPDATE notifications
                SET is_read = 1
                WHERE id = ?
                  AND user_id = ?
                """,
                notificationId,
                getCurrentUser().getId()
        );
    }

    /*
     * Mark every notification belonging to
     * the logged-in user as read.
     */
    @Override
    public void markAllAsRead() {

        jdbcTemplate.update(
                """
                UPDATE notifications
                SET is_read = 1
                WHERE user_id = ?
                  AND is_read = 0
                """,
                getCurrentUser().getId()
        );
    }

    /*
     * Internal notification insert.
     */
    private void insertNotification(
            Long userId,
            String type,
            String title,
            String message,
            String referenceType,
            Long referenceId
    ) {

        if (userId == null) {
            return;
        }

        jdbcTemplate.update(
                """
                INSERT INTO notifications (
                    user_id,
                    type,
                    title,
                    message,
                    reference_type,
                    reference_id
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                userId,
                type,
                title,
                message,
                referenceType,
                referenceId
        );
    }

    /*
     * Send task assignment notification to
     * the selected supervisor and farmers.
     */
    @Override
    public void notifyTaskAssigned(
            Long taskId,
            String taskTitle,
            Long supervisorId,
            Collection<Long> workerIds
    ) {

        Set<Long> recipientIds =
                new LinkedHashSet<>();

        if (supervisorId != null) {
            recipientIds.add(supervisorId);
        }

        if (workerIds != null) {
            recipientIds.addAll(workerIds);
        }

        for (Long userId : recipientIds) {

            if (userId == null) {
                continue;
            }

            /*
             * If a user_settings record does not
             * exist, task notification is enabled
             * by default.
             */
            Boolean taskNotificationsEnabled =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT COALESCE(
                                (
                                    SELECT task_reminders
                                    FROM user_settings
                                    WHERE user_id = ?
                                ),
                                1
                            )
                            """,
                            Boolean.class,
                            userId
                    );

            if (Boolean.TRUE.equals(
                    taskNotificationsEnabled
            )) {

                insertNotification(
                        userId,
                        "TASK_ASSIGNED",
                        "New task assigned",
                        "You have been assigned to task: "
                                + taskTitle,
                        "TASK",
                        taskId
                );
            }
        }
    }

    /*
     * Send attendance notification to:
     *
     * 1. The relevant farmer
     * 2. Supervisors assigned to the farm
     * 3. All enabled administrators
     */
    @Override
    public void notifyAttendanceMarked(
            Long attendanceId,
            Long farmId,
            Long workerId,
            String workerName,
            String action
    ) {

        Set<Long> recipientIds =
                new LinkedHashSet<>();

        if (workerId != null) {
            recipientIds.add(workerId);
        }

        if (farmId != null) {

            List<Long> managers =
                    jdbcTemplate.queryForList(
                            """
                            SELECT DISTINCT u.id
                            FROM users u
                            LEFT JOIN farm_assignments fa
                                ON fa.user_id = u.id
                            WHERE u.enabled = 1
                              AND (
                                  u.role = 'ADMIN'
                                  OR (
                                      u.role = 'SUPERVISOR'
                                      AND fa.farm_id = ?
                                  )
                              )
                            """,
                            Long.class,
                            farmId
                    );

            recipientIds.addAll(managers);

        } else {

            List<Long> administrators =
                    jdbcTemplate.queryForList(
                            """
                            SELECT id
                            FROM users
                            WHERE role = 'ADMIN'
                              AND enabled = 1
                            """,
                            Long.class
                    );

            recipientIds.addAll(administrators);
        }

        boolean isCheckOut =
                "CHECK_OUT".equalsIgnoreCase(action);

        String notificationType =
                isCheckOut
                        ? "ATTENDANCE_CHECK_OUT"
                        : "ATTENDANCE_CHECK_IN";

        String title =
                isCheckOut
                        ? "Attendance check-out"
                        : "Attendance check-in";

        String safeWorkerName =
                workerName == null ||
                        workerName.isBlank()
                        ? "Worker"
                        : workerName;

        String message =
                isCheckOut
                        ? safeWorkerName
                        + " checked out successfully."
                        : safeWorkerName
                        + " marked attendance successfully.";

        for (Long userId : recipientIds) {

            insertNotification(
                    userId,
                    notificationType,
                    title,
                    message,
                    "ATTENDANCE",
                    attendanceId
            );
        }
    }
}
