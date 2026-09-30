package com.agrofarm.api.dto.reports;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportAiChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String activeTab;

    private Long farmId;

    private String farmName;

    private LocalDate from;

    private LocalDate to;

    private String language;

    private List<Map<String, String>> history;
}
