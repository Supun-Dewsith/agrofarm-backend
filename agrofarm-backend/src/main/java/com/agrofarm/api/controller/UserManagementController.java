package com.agrofarm.api.controller;

import com.agrofarm.api.dto.auth.AvailableFarmsResponse;
import com.agrofarm.api.dto.user.UpdateUserRequest;
import com.agrofarm.api.dto.user.UserData;
import com.agrofarm.api.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserData> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UpdateUserRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request, file));
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
