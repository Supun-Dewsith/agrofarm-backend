package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.disease.DiseaseAnalysisContext;
import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;
import com.agrofarm.api.repository.DiseaseDetectionRepository;
import com.agrofarm.api.service.DiseaseAnalysisService;
import com.agrofarm.api.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiseaseAnalysisServiceImpl implements DiseaseAnalysisService {

    private final RestClient restClient;
    private final StorageService storageService;
    private final DiseaseDetectionRepository diseaseDetectionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DiseaseAnalysisServiceImpl(
            RestClient diseaseAnalysisRestClient,
            StorageService storageService,
            DiseaseDetectionRepository diseaseDetectionRepository,
            ObjectMapper objectMapper
    ) {
        this.restClient = diseaseAnalysisRestClient;
        this.storageService = storageService;
        this.diseaseDetectionRepository = diseaseDetectionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String analyzeLeafImage(MultipartFile file, String language, String crop) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        body.add("file", fileResource);
        body.add("language", (language != null && !language.isBlank()) ? language : "en");
        if (crop != null && !crop.isBlank()) {
            body.add("crop", crop.trim());
        }

        return restClient.post()
                .uri("/analyze-leaf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String analyzeLeafImage(MultipartFile file, String language) throws IOException {
        return analyzeLeafImage(file, language, null);
    }

    @Override
    public String analyzeLeafImage(MultipartFile file) throws IOException {
        return analyzeLeafImage(file, "en", null);
    }

    @Override
    public DiseaseChatResponse chatAboutDetection(DiseaseChatRequest request) {
        return restClient.post()
                .uri("/disease-chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(DiseaseChatResponse.class);
    }

    @Override
    public DiseaseDetectionResponse analyzeAndSave(MultipartFile file, String language, String crop, Long userId) throws IOException {
        // 1. Upload leaf image to Supabase Storage via StorageService
        String imageUrl = storageService.uploadFile(file);

        // 2. Perform leaf disease analysis via Python AI microservice (with optional crop context)
        String analysisJson = analyzeLeafImage(file, language, crop);

        // 3. Deserialize JSON result into DiseaseAnalysisContext
        DiseaseAnalysisContext context = objectMapper.readValue(analysisJson, DiseaseAnalysisContext.class);

        // 4. Persist detection record into MySQL database
        Long detectionId = diseaseDetectionRepository.save(userId, imageUrl, context);

        // 5. Return complete detection response
        return new DiseaseDetectionResponse(
                detectionId,
                userId,
                null,
                null,
                imageUrl,
                context.plantName(),
                context.isHealthy(),
                context.diseaseName(),
                context.overallConfidence(),
                context.generalDescription(),
                context.detectedSpots(),
                context.possibleTreatments(),
                context.preventionTips(),
                context.pathogenType(),
                context.scientificName(),
                context.optimalTemperature(),
                context.humidityLevel(),
                context.weatherCorrelation(),
                context.lifeCycleStages(),
                context.yieldLossRisk(),
                context.yieldLossPercentage(),
                context.spreadSpeed(),
                context.harvestImpact(),
                context.ipmMatrix(),
                context.probabilityDistribution(),
                context.referenceImageUrl(),
                LocalDateTime.now()
        );
    }

    @Override
    public List<DiseaseDetectionResponse> getRecentDetections(Long userId, int limit) {
        return diseaseDetectionRepository.findRecentByUserId(userId, limit);
    }

    @Override
    public DiseaseDetectionResponse getDetectionById(Long id, Long userId) {
        return diseaseDetectionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Detection record not found for id: " + id));
    }

    @Override
    public boolean linkDetectionToPlot(Long id, Long farmId, Long fieldId, Long userId) {
        return diseaseDetectionRepository.linkFarmAndField(id, farmId, fieldId, userId);
    }
}