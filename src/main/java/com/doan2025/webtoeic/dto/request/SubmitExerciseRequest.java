package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class SubmitExerciseRequest {
    private Long submitId;
    private Long notificationId;
    private String linkUrl;
    private Boolean isActive;
    private Boolean isDelete;

}
