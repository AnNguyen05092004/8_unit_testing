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
public class BankResponse {
    private Long id;
    private String url;
    private String questionBankTitle;
    private List<QuestionResponse> questions;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private Boolean isActive;
    private Boolean isDelete;
    private Date createdAt;
    private Date updatedAt;
}
