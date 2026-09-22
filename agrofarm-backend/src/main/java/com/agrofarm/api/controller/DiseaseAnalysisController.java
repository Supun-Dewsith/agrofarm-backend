package com.agrofarm.api.controller;

import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;
import com.agrofarm.api.service.DiseaseAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    public ResponseEntity<String> analyzeLeaf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "language", required = false, defaultValue = "en") String language
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Please upload a valid image file.\"}");
        }

        try {
            String analysisResult = diseaseAnalysisService.analyzeLeafImage(file, language);
            return ResponseEntity.ok(analysisResult);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Failed to process image: " + e.getMessage() + "\"}");
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