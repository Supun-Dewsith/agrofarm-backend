package com.agrofarm.api.controller;

import com.agrofarm.api.dto.notification.NotificationResponse;
import com.agrofarm.api.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /*
     * GET /api/v1/notifications?limit=30
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications(
            @RequestParam(defaultValue = "30")
            int limit
    ) {

        return ResponseEntity.ok(
                notificationService.getMine(limit)
        );
    }

    /*
     * GET /api/v1/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>>
    getUnreadCount() {

        return ResponseEntity.ok(
                Map.of(
                        "count",
                        notificationService
                                .getUnreadCount()
                )
        );
    }

    /*
     * PUT /api/v1/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id
    ) {

        notificationService.markAsRead(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    /*
     * PUT /api/v1/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {

        notificationService.markAllAsRead();

        return ResponseEntity
                .noContent()
                .build();
    }
}