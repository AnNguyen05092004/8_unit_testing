package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    private Long id;
    private String questionContent;
    private String category;
    private String difficulty;
    private List<AnswerResponse> answers;
    private ExplanationQuestionResponse explanation;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
}
