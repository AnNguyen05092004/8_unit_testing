package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.ECategoryCourse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Long price;
    private String thumbnailUrl;
    private String categoryName;
    private Date updatedAt;
    private Date createdAt;
    private Boolean isDelete;
    private Boolean isActive;
    private Boolean isBought;
    private String authorName;
    private String createdByName;
    private String updatedByName;
    private UserResponse author;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private List<LessonResponse> lessons;
    private Long isOrdered;
    private String statusOrder;

    public CourseResponse(Long id, String title, String description, Long price, String thumbnailUrl, ECategoryCourse category,
                          Date updatedAt, Date createdAt, Boolean isDelete, Boolean isActive, String authorName, String createdByName,
                          String updatedByName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryName = category != null ? category.getName() : null;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.isDelete = isDelete;
        this.isActive = isActive;
        this.authorName = authorName;
        this.createdByName = createdByName;
        this.updatedByName = updatedByName;
    }

    public CourseResponse(Long id, String title, String description, Long price, String thumbnailUrl,
                          ECategoryCourse category, Date updatedAt, Date createdAt, Boolean isDelete, Boolean isActive,
                          String authorName, String createdByName, String updatedByName, Boolean isBought, Long isOrdered,
                          String statusOrder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryName = category != null ? category.getName() : null;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.isDelete = isDelete;
        this.isActive = isActive;
        this.authorName = authorName;
        this.createdByName = createdByName;
        this.updatedByName = updatedByName;
        this.isBought = isBought;
        this.isOrdered = isOrdered;
        this.statusOrder = statusOrder;
    }
}
