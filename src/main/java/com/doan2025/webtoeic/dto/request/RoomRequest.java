package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class RoomRequest {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Boolean isDelete;
}
