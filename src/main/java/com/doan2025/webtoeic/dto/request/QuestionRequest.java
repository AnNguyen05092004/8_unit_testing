package com.doan2025.webtoeic.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class QuestionRequest {
    private Long id;
    private String questionContent;
    private String category;
    private String difficulty;
    private List<AnswerRequest> answers;
    private ExplanationQuestionRequest explanation;
    private boolean isActive;
    private boolean isDelete;
}
