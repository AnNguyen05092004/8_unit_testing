package com.doan2025.webtoeic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private String videoUrl;
    private Double duration;
    private Integer orderIndex;
    private Boolean isPreviewAble;
    private CourseResponse courseId;
    private Boolean isBought;
    private Boolean isDelete;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private String createdByName;
    private String updatedByName;
    private List<AttachDocumentLessonResponse> attachDocumentLessons;

    public LessonResponse(Long id, String title, String content, String videoUrl, Double duration, Integer orderIndex,
                          Boolean isPreviewAble, Boolean isDeleted, Boolean isActive,
                          Date createdAt, Date updatedAt, String createdByName, String updatedByName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.isPreviewAble = isPreviewAble;
        this.isDelete = isDeleted;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdByName = createdByName;
        this.updatedByName = updatedByName;
    }

    public LessonResponse(Long id, String title, String content, String videoUrl, Double duration, Integer orderIndex,
                          Boolean isPreviewAble, Boolean isDeleted, Boolean isActive,
                          Date createdAt, Date updatedAt, String createdByName, String updatedByName, Boolean isBought) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.orderIndex = orderIndex;
        this.isPreviewAble = isPreviewAble;
        this.isDelete = isDeleted;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdByName = createdByName;
        this.updatedByName = updatedByName;
        this.isBought = isBought;
    }
}
