package com.agrofarm.api.controller;

import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;
import com.agrofarm.api.security.AuthenticatedUser;
import com.agrofarm.api.service.DiseaseAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disease")
public class DiseaseAnalysisController {

    private final DiseaseAnalysisService diseaseAnalysisService;

    public DiseaseAnalysisController(DiseaseAnalysisService diseaseAnalysisService) {
        this.diseaseAnalysisService = diseaseAnalysisService;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> analyzeLeaf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false, defaultValue = "en") String language,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a valid image file."));
        }

        try {
            Long userId = (user != null && user.getId() != null) ? user.getId() : 1L;
            DiseaseDetectionResponse response = diseaseAnalysisService.analyzeAndSave(file, language, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process image: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DiseaseDetectionResponse>> getHistory(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit
    ) {
        Long userId = (user != null && user.getId() != null) ? user.getId() : 1L;
        List<DiseaseDetectionResponse> history = diseaseAnalysisService.getRecentDetections(userId, limit);
        return ResponseEntity.ok(history != null ? history : Collections.emptyList());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDetectionById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        try {
            Long userId = (user != null && user.getId() != null) ? user.getId() : 1L;
            DiseaseDetectionResponse response = diseaseAnalysisService.getDetectionById(id, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiseaseChatResponse> chat(@RequestBody DiseaseChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(diseaseAnalysisService.chatAboutDetection(request));
    }
}