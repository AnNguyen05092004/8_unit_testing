package com.doan2025.webtoeic.dto;

import lombok.Data;

@Data
public class SearchSubmitExerciseDto {
    private String searchString;
    private Long notificationId;
}
