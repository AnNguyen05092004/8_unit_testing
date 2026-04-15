package com.doan2025.webtoeic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OverviewStudentAttendance {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long present;
    private Long absent;
    private Long late;
}
