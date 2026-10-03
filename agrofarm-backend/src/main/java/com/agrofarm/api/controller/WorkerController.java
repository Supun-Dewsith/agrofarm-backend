package com.agrofarm.api.controller;


import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.workers.WorkerRequest;
import com.agrofarm.api.service.CropService;
import com.agrofarm.api.service.WorkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

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


    @PostMapping("/delete")
    public ResponseEntity<?> deleteWorker(@RequestBody Long ID){
        boolean deleteWorker = workerService.deleteWorker(ID);

        if (!deleteWorker) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User deleted successfully");

    }

}
