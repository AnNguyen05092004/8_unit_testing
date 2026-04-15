package com.doan2025.webtoeic.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SearchScheduleSto {
    private List<Long> classId;
    private List<Long> teacherId;
    private List<String> status;
    private Date fromDate;
    private Date toDate;
}
