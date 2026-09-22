package com.agrofarm.api.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;


public interface DiseaseAnalysisService {
    String analyzeLeafImage(MultipartFile file, String language) throws IOException;

    default String analyzeLeafImage(MultipartFile file) throws IOException {
        return analyzeLeafImage(file, "en");
    }

    DiseaseChatResponse chatAboutDetection(DiseaseChatRequest request);
}
