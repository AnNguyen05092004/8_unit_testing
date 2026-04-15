package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachDocumentLessonResponse {
    private Long id;
    private String linkUrl;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
}
