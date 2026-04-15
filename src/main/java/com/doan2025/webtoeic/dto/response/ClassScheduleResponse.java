package com.doan2025.webtoeic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassScheduleResponse {
    private Long id;
    private ClassResponse clazz;
    private String title;
    private Date startAt;
    private Date endAt;
    private String status;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private RoomResponse room;
}
