package com.agrofarm.api.repository;

import com.agrofarm.api.dto.disease.DiseaseAnalysisContext;
import com.agrofarm.api.dto.disease.DiseaseDetectionResponse;

import java.util.List;
import java.util.Optional;

public interface DiseaseDetectionRepository {
    Long save(Long userId, String imageUrl, DiseaseAnalysisContext context);
    List<DiseaseDetectionResponse> findRecentByUserId(Long userId, int limit);
    Optional<DiseaseDetectionResponse> findByIdAndUserId(Long id, Long userId);
}
