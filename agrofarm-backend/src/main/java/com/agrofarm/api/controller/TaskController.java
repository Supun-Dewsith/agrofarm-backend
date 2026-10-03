package com.agrofarm.api.controller;

import com.agrofarm.api.dto.task.TaskDashboardResponse;
import com.agrofarm.api.dto.task.TaskOptionsResponse;
import com.agrofarm.api.dto.task.TaskRequest;
import com.agrofarm.api.service.TaskService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/options")
    public ResponseEntity<TaskOptionsResponse> getOptions(
            @RequestParam(
                    name = "farmId",
                    required = false
            )
            Long farmId
    ) {
        return ResponseEntity.ok(
                taskService.getOptions(farmId)
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
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    @PostMapping
    public ResponseEntity<TaskDashboardResponse> createTask(
            @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(
                taskService.createTask(request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDashboardResponse> updateTask(
            @PathVariable("id") Long id,
            @RequestBody TaskRequest request
    ) {
        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }

    // Uses PUT so no PATCH CORS configuration is needed.
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskDashboardResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status
    ) {
        return ResponseEntity.ok(
                taskService.updateStatus(id, status)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("id") Long id
    ) {
        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}