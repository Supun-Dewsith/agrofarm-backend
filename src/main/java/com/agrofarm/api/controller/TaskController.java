package com.agrofarm.api.controller;

import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;
import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.service.TaskService;
import jakarta.validation.Valid;
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
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request) {


        TaskResponse response =
                taskService.createTask(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }





    /**
     * Get All Tasks
     * GET: /api/v1/tasks
     *
     * Optional filter:
     * GET: /api/v1/tasks?status=PENDING
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) String status) {


        List<TaskResponse> tasks =
                taskService.getAllTasks(status);


        return ResponseEntity.ok(tasks);
    }





    /**
     * Dashboard Summary
     * GET: /api/v1/tasks/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<TaskDashboardResponse> getDashboardSummary() {


        TaskDashboardResponse response =
                taskService.getTaskDashboardSummary();


        return ResponseEntity.ok(response);
    }





    /**
     * Get Task By ID
     * GET: /api/v1/tasks/{id}
     */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {


        TaskResponse response =
                taskService.getTaskById(id);


        return ResponseEntity.ok(response);
    }





    /**
     * Update Task
     * PUT: /api/v1/tasks/{id}
     */
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {


        TaskResponse response =
                taskService.updateTask(id, request);


        return ResponseEntity.ok(response);
    }





    /**
     * Delete Task
     * DELETE: /api/v1/tasks/{id}
     */
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id) {


        taskService.deleteTask(id);


        return ResponseEntity.ok(
                "Task deleted successfully."
        );
    }

}