package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShareQuizResponse {
    private Long sharedQuizId;

    private Date startAt;

    private Date endAt;

    private ClassResponse clazz;

    private QuizResponse quiz;

    private UserResponse createdBy;

    private UserResponse updatedBy;

    private Boolean isDelete;

    private Boolean isActive;

    private Date createdAt;

    private Date updatedAt;
}
