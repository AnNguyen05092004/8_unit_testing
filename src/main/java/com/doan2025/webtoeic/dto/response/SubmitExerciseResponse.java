package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExerciseResponse {
    private Long id;

    private String linkUrl;

    private Boolean isActive;

    private Boolean isDelete;

    private Date createdAt;

    private Date updatedAt;

    private UserResponse createdBy;
}
