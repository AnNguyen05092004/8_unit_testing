package com.doan2025.webtoeic.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class SearchMemberInClassDto {
    private Long classId;
    private String searchString;
    private Date joinDate;
    private List<String> status;
}
