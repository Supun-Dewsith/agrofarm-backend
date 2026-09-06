package com.agrofarm.api.controller;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskRequest;
import com.agrofarm.api.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDashboardResponse> createTask(
            @RequestBody TaskRequest request
    ) {

        return ResponseEntity.ok(
                taskService.createTask(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<TaskDashboardResponse>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDashboardResponse> getTaskById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDashboardResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest request
    ) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDashboardResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        return ResponseEntity.ok(
                taskService.updateStatus(id, status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id
    ) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}
