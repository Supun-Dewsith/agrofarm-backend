package com.agrofarm.api.controller;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<UserData>> getAll(){
        return ResponseEntity.ok(userManagementService.getAll());
    }

    @GetMapping("/farms")
    public ResponseEntity<AvailableFarmsResponse> getAllFarms(){
        return ResponseEntity.ok(userManagementService.getAvailableFarms());
    }

    @GetMapping("/farms/{id}")
    public ResponseEntity<AvailableFarmsResponse> getAllFarmsByOwner(@PathVariable Long id){
        return ResponseEntity.ok(userManagementService.getAvailableFarmsByOwnerId(id));
    }
}
