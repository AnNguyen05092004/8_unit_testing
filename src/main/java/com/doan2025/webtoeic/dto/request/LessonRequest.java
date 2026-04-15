package com.doan2025.webtoeic.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequest {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private Double duration; // truong auto fill
    private Integer orderIndex;
    private Boolean isPreviewAble;
    private Long courseId;
    private Boolean isActive;
    private Boolean isDelete;
    private List<String> documentUrls;
}
