package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SearchClassDto {
    private String searchString; // tìm theo title, description, subject,
    private Long idTeacher;
    private List<String> statusClass;
    private Date fromDate;
    private Date toDate;
}
