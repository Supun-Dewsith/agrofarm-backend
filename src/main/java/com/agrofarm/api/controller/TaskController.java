package com.agrofarm.api.controller;


import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;

import com.agrofarm.api.service.TaskService;


import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


import java.util.List;



@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {



    private final TaskService taskService;



    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @RequestBody TaskRequest request
    ){


        return ResponseEntity.ok(

                taskService.create(request)

        );

    }




    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(){


        return ResponseEntity.ok(

                taskService.getAllTasks()

        );

    }





    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable Long id
    ){


        return ResponseEntity.ok(

                taskService.getTaskById(id)

        );

    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ){


        taskService.deleteTask(id);


        return ResponseEntity.ok().build();

    }


}