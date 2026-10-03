package com.agrofarm.api.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;

import java.util.List;

public interface DiseaseAnalysisService {
    String analyzeLeafImage(MultipartFile file, String language) throws IOException;

    default String analyzeLeafImage(MultipartFile file) throws IOException {
        return analyzeLeafImage(file, "en");
    }

    DiseaseChatResponse chatAboutDetection(DiseaseChatRequest request);

    DiseaseDetectionResponse analyzeAndSave(MultipartFile file, String language, Long userId) throws IOException;

    List<DiseaseDetectionResponse> getRecentDetections(Long userId, int limit);

    DiseaseDetectionResponse getDetectionById(Long id, Long userId);

    boolean linkDetectionToPlot(Long id, Long farmId, Long fieldId, Long userId);
}
