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
public class ClassResponse {
    private long id;
    private String name;
    private String description;
    private String subject;
    private String title;
    private String status;
    private UserResponse teacher;
    private Date createdAt;
    private Date updatedAt;
    private String createdByName;
    private String updatedByName;
}
