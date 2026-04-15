package com.doan2025.webtoeic.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class OverviewStatisticAttendance {
    private Long scheduleId;
    private Long classId;
    private String title;
    private Date startAt;
    private Date endAt;
    private Long present;
    private Long absent;
    private Long late;

    public OverviewStatisticAttendance(Long scheduleId, Long classId, String title, Date startAt, Date endAt, Long present, Long absent, Long late) {
        this.scheduleId = scheduleId;
        this.classId = classId;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.present = present;
        this.absent = absent;
        this.late = late;
    }
}
