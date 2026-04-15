package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class ExplanationQuestionRequest {
    private Long id;
    private String explanationVietnamese;
    private String explanationEnglish;
    private Boolean isActive;
    private Boolean isDelete;
}
