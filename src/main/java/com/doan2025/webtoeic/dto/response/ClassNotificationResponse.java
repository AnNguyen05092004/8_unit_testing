package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassNotificationResponse {
    private Long id;
    private String description;
    private String typeNotification;
    private Date fromDate;
    private Date toDate;
    private Boolean isPin;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private List<AttachDocumentClassResponse> attachDocumentClasses;
}
