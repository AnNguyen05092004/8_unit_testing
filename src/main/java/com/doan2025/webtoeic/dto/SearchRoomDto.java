package com.doan2025.webtoeic.dto;

import lombok.Data;

@Data
public class SearchRoomDto {
    private String searchString;
    private Boolean isActive;
    private Boolean isDelete;
}
