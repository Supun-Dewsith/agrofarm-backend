package com.agrofarm.api.controller;

import com.agrofarm.api.dto.FarmRequest;
import com.agrofarm.api.dto.FarmResponse;
import com.agrofarm.api.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FarmController {

    private final FarmService farmService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FarmResponse> createFarm(
            @Valid @RequestBody FarmRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(farmService.createFarm(request));
    }

    @GetMapping
    public ResponseEntity<List<FarmResponse>> getAllFarms() {

        return ResponseEntity.ok(
                farmService.getAllFarms()
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<FarmResponse> getFarmById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                farmService.getFarmById(id)
        );
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FarmResponse> updateFarm(
            @PathVariable Long id,
            @Valid @RequestBody FarmRequest request
    ) {

        return ResponseEntity.ok(
                farmService.updateFarm(id, request)
        );
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteFarm(
            @PathVariable Long id
    ) {

        farmService.deleteFarm(id);

        return ResponseEntity.ok(
                "Farm deleted successfully."
        );
    }
}