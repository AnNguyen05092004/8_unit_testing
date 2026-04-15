package com.doan2025.webtoeic.dto.request;

import lombok.Data;

@Data
public class RangeTopicRequest {

    private Long rangeTopicId;
    private String content;
    private String description;
    private String vietnamese;
    private Boolean isDelete;
    private Boolean isActive;
}
