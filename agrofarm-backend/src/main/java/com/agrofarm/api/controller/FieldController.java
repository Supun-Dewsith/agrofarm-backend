package com.agrofarm.api.controller;

import com.agrofarm.api.dto.FieldRequest;
import com.agrofarm.api.dto.FieldResponse;
import com.agrofarm.api.service.FieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FieldController {

    private final FieldService fieldService;

    @PostMapping
    public ResponseEntity<FieldResponse> createField(
            @Valid @RequestBody FieldRequest request
    ) {

        FieldResponse response =
                fieldService.createField(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<FieldResponse>> getAllFields() {

        return ResponseEntity.ok(
                fieldService.getAllFields()
        );
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<FieldResponse> getFieldById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                fieldService.getFieldById(id)
        );
    }

    @GetMapping("/farm/{farmId:\\d+}")
    public ResponseEntity<List<FieldResponse>> getFieldsByFarm(
            @PathVariable Long farmId
    ) {

        return ResponseEntity.ok(
                fieldService.getFieldsByFarm(farmId)
        );
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<FieldResponse> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldRequest request
    ) {

        return ResponseEntity.ok(
                fieldService.updateField(id, request)
        );
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> deleteField(
            @PathVariable Long id
    ) {

        fieldService.deleteField(id);

        return ResponseEntity.ok(
                "Field deleted successfully."
        );
    }
}