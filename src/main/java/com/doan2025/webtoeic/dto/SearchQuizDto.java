package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SearchQuizDto {
    private String searchString;
    private Date fromDate;
    private Date toDate;
}
