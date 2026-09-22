package com.agrofarm.api.service.impl;

import com.agrofarm.api.dto.disease.DiseaseChatRequest;
import com.agrofarm.api.dto.disease.DiseaseChatResponse;
import com.agrofarm.api.service.DiseaseAnalysisService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DiseaseAnalysisServiceImpl implements DiseaseAnalysisService {

    private final RestClient restClient;

    public DiseaseAnalysisServiceImpl(RestClient diseaseAnalysisRestClient) {
        this.restClient = diseaseAnalysisRestClient;
    }

    @Override
    public String analyzeLeafImage(MultipartFile file, String language) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        body.add("file", fileResource);
        body.add("language", (language != null && !language.isBlank()) ? language : "en");

        return restClient.post()
                .uri("/analyze-leaf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String analyzeLeafImage(MultipartFile file) throws IOException {
        return analyzeLeafImage(file, "en");
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
}