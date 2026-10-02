package com.agrofarm.api.service;

import com.agrofarm.api.dto.notification.NotificationResponse;

import java.util.Collection;
import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMine(int limit);

    long getUnreadCount();

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void notifyTaskAssigned(
            Long taskId,
            String taskTitle,
            Long supervisorId,
            Collection<Long> workerIds
    );

    void notifyAttendanceMarked(
            Long attendanceId,
            Long farmId,
            Long workerId,
            String workerName,
            String action
    );

    void notifyPasswordResetRequested(String usernameOrEmail);
}