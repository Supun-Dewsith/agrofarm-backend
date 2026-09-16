package com.agrofarm.api.controller;


import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.service.CropService;
import com.agrofarm.api.service.WorkerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController

@RequestMapping("/api/worker")
public class WorkerController {

    private CropService cropService;
    private WorkerService workerService;

    public WorkerController(CropService cropService, WorkerService workerService){

        this.cropService = cropService;
        this.workerService = workerService;
    }

    @GetMapping("/workers")
    public List<WorkerRequest> getWorkerDetails(){



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);

        if (role.equals("SUPERVISOR")) {

            return workerService.SupervisorRequest(currentUsername);

        } else if (role.equals("ADMIN")) {
            return workerService.AdminRequest(currentUsername);
        } else {
            return Collections.emptyList();
        }

       


    }


    @GetMapping("/farms")
    public List<FarmIDs> getFarmDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);


        if (role.equals("SUPERVISOR")) {

            return cropService.getFarmDetails(currentUsername);
        } else if (role.equals("ADMIN")) {
            return cropService.getAdminFarmDetails(currentUsername);
        } else {
            return Collections.emptyList();
        }
    }

}
