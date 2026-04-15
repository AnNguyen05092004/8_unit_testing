package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class ScoreScaleRequest {
    private Long scoreScaleId;
    private String title;
    private Integer fromScore;
    private Integer toScore;
    private Boolean isActive;
    private Boolean isDelete;
}
