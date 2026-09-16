package com.agrofarm.api.dto.workers;


import lombok.Data;

@Data
public class WorkerRequest {

        Long id;
        String name;
        String phone;
        String email;
        String farm;
        String[] tasks;
        String status;
        String joinDate;
        String profilePicture;
        String role;

}
