package com.agrofarm.api.service;


import com.agrofarm.api.dto.TaskRequest;
import com.agrofarm.api.dto.TaskResponse;

import java.util.List;


public interface TaskService {


    TaskResponse create(TaskRequest request);


    List<TaskResponse> getAllTasks();


    TaskResponse getTaskById(Long id);


    void deleteTask(Long id);

}