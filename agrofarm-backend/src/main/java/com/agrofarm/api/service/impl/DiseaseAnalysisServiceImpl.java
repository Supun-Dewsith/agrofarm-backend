package com.agrofarm.api.service.impl;

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
    public String analyzeLeafImage(MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        body.add("file", fileResource);

        return restClient.post()
                .uri("/analyze-leaf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);
    }
}