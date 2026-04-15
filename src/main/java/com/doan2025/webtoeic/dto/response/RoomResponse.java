package com.doan2025.webtoeic.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class RoomResponse {
    private Long id;
    private String name;
    private String description;
    private String isActive;
    private String isDelete;
    private Date createdAt;
    private Date updatedAt;
}
