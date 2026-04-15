package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.ECategoryPost;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String themeUrl;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isActive;
    private Boolean isDelete;
    private Boolean isOwn;
    private String category;
    private String author;

    public PostResponse(Long id, String title, String content, String themeUrl, Date createdAt, Date updatedAt, Boolean isActive, Boolean isDelete, ECategoryPost category, Boolean isOwn, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.themeUrl = themeUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.isDelete = isDelete;
        this.category = category == null ? null : category.name();
        this.isOwn = isOwn;
        this.author = author;
    }

    public PostResponse(Long id, String title, String content, String themeUrl, Date createdAt, Date updatedAt, Boolean isActive, Boolean isDelete, ECategoryPost category, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.themeUrl = themeUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.isDelete = isDelete;
        this.category = category == null ? null : category.name();
        this.author = author;
    }

    public PostResponse(Long id, String title, String content, String themeUrl, ECategoryPost category, Date createdAt, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.themeUrl = themeUrl;
        this.createdAt = createdAt;
        this.category = category == null ? null : category.name();
        this.author = author;
    }
}
