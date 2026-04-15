package com.doan2025.webtoeic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClassRequest {
    private long id;
    private String name;
    private Long roomId;
    private String description;
    private String subject;
    private String title;
    private List<Long> memberIds;
    private Integer status;
    private Long teacher;
    private Date createdAt;
    private Date updatedAt;
    private String createdByName;
    private String updatedByName;
}
