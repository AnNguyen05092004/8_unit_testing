package com.doan2025.webtoeic.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class QuizRequest {
    private Long id;
    private String title;
    private String description;
    private List<Long> idQuestions;
    private Boolean isActive;
    private Boolean isDelete;
    private int status;
}
