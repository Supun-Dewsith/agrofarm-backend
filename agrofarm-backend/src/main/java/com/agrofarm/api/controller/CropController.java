package com.agrofarm.api.controller;


import com.agrofarm.api.dto.FarmIDs;
import com.agrofarm.api.service.CropService;
import com.agrofarm.api.service.impl.CropServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropServiceImpl cropService;

    public CropController(CropServiceImpl cropService){

        this.cropService = cropService;
    }


    @GetMapping("/farms")
    public List<FarmIDs> getFarmDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return cropService.getFarmDetails(currentUsername);

    }





    @PostMapping("/crops")
    public void getCropDetails(@RequestBody Long id){

    }
}
