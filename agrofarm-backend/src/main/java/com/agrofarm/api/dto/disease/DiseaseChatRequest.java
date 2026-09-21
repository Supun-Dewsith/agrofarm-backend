package com.agrofarm.api.dto.disease;

import java.util.List;

public record DiseaseChatRequest(
        String question,
        DiseaseAnalysisContext analysis,
        List<DiseaseChatMessage> history
) {}