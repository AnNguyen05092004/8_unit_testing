package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchQuestionDto {
    private String searchString;
    private List<Long> scoreScales;
    private List<Long> rangeTopics;
    private Long idQuestionBank;
    private Boolean isActive;
    private Boolean isDelete;

}
