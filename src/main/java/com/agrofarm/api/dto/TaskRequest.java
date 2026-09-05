package com.agrofarm.api.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class TaskRequest {


    private String title;


    private String description;


    private String priority;


    private Long farmId;


    private Long fieldId;


    private Long supervisorId;


    private List<Long> workerIds;


    private LocalDate dueDate;

}