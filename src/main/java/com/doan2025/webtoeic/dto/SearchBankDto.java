package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchBankDto {
    private String searchString;
    private List<Long> createByIds;
    private Boolean isActive;
    private Boolean isDelete;
}
