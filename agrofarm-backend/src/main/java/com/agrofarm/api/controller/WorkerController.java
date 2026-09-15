package com.agrofarm.api.controller;


import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.service.CropService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Controller

@RequestMapping("/api/worker")
public class WorkerController {

    private CropService cropService;

    public WorkerController(CropService cropService){
        this.cropService = cropService;
    }

    @GetMapping("/workers")
    public WorkerRequest getWorkerDetails(){



        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);

        if (role.equals("SUPERVISOR")) {

            return cropService.getFarmDetails(currentUsername);
        }
        else if (role.equals("ADMIN")) {
            return cropService.getAdminFarmDetails(currentUsername);
        }
        else {
            return Collections.emptyList();
        }


    }

}
