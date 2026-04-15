package com.doan2025.webtoeic.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class PostRequest {
    private Long id;
    private String title;
    private String content;
    private String themeUrl;
    private Integer categoryId;
    private Date createdAt;
    private Date updatedAt;
    // for role manager
    private Boolean isActive;
    private Boolean isDelete;
}
