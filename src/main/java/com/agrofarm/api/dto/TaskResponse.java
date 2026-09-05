package com.agrofarm.api.dto;


import lombok.Data;

import java.time.LocalDate;
import java.util.List;


@Data
public class TaskResponse {


    private Long id;


    private String title;


    private String description;


    private String priority;


    private String status;



    // Farm

    private Long farmId;

    private String farmName;



    // Field optional

    private Long fieldId;

    private String fieldName;



    // Supervisor

    private Long supervisorId;

    private String supervisorName;



    // Workers

    private List<WorkerResponse> workers;



    private LocalDate dueDate;


}