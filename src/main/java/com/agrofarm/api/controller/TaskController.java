package com.agrofarm.api.controller;

import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;
import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    /**
     * Create Task
     * POST: /api/v1/tasks
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {

        TaskResponse response = taskService.createTask(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get All Tasks
     * GET: /api/v1/tasks
     * GET: /api/v1/tasks?status=PENDING
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(taskService.getAllTasks(status));
    }

    /**
     * Get Task By ID
     * GET: /api/v1/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Update Task
     * PUT: /api/v1/tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest request) {

        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /**
     * Delete Task
     * DELETE: /api/v1/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.ok("Task deleted successfully.");
    }

    /**
     * Dashboard Summary
     * GET: /api/v1/tasks/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<TaskDashboardResponse> getDashboardSummary() {

        return ResponseEntity.ok(taskService.getTaskDashboardSummary());
    }

}