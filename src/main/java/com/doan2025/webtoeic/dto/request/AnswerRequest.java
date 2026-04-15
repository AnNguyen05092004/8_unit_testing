package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long id;
    private String content; // Nội dung câu trả lời
    private Boolean correct;
}
