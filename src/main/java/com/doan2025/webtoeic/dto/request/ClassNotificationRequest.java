package com.doan2025.webtoeic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ClassNotificationRequest {
    private Long classNotificationId;
    private Long classId;
    private String description;
    private int typeNotification;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date toDate;
    private Boolean isPin;
    private Boolean isActive;
    private Boolean isDelete;
    private List<String> urlAttachment;
}
