package com.agrofarm.api.dto.disease;

import java.util.List;

public record DiseaseChatRequest(
        String question,
        DiseaseAnalysisContext analysis,
        List<DiseaseChatMessage> history,
        String language
) {
    public DiseaseChatRequest(String question, DiseaseAnalysisContext analysis, List<DiseaseChatMessage> history) {
        this(question, analysis, history, "en");
    }

    @Override
    public String language() {
        return (language == null || language.isBlank()) ? "en" : language;
    }
}