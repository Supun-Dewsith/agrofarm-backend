package com.agrofarm.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DiseaseAnalysisService {
    String analyzeLeafImage(MultipartFile file) throws IOException;
}
