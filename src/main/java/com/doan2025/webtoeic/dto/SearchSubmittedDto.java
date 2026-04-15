package com.doan2025.webtoeic.dto;

import lombok.Data;

@Data
public class SearchSubmittedDto {
    private String searchString;
    private Long fromScore;
    private Long toScore;
}
