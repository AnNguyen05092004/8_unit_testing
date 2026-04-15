package com.doan2025.webtoeic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class DetailStatisticAttendance {
    private Long userId;
    private Long classId;
    private Long scheduleId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private boolean present;
    private boolean absent;
    private boolean late;
    private Date checkInDate;
    private Long attendanceId;

}
