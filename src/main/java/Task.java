package com.agrofarm.api.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class Task {


    private Long id;


    private String title;


    private String description;


    private String priority;


    private String status;


    private Long farmId;


    private Long fieldId;


    private Long supervisorId;


    private LocalDate dueDate;


    private LocalDateTime createdAt;

}