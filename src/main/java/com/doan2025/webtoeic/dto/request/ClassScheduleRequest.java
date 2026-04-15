package com.doan2025.webtoeic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ClassScheduleRequest {
    private Long classId;
    private Long classScheduleId;
    private int status;
    private String title;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startAt;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endAt;
    private Boolean isActive;
    private Boolean isDelete;
    private Long roomId;
}
