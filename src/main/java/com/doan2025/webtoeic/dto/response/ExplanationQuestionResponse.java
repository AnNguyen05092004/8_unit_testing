package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExplanationQuestionResponse {
    private Long id;
    private String explanationVietnamese;
    private String explanationEnglish;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
}
