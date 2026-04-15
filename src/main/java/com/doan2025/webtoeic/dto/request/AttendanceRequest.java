package com.doan2025.webtoeic.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class AttendanceRequest {
    private Long classId;
    private Long attendanceId;
    private Long studentId;
    private Long scheduleId;
    private Date checkIn;
    private int attendanceStatus;
}
