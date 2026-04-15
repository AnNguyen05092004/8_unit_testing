package com.doan2025.webtoeic.dto.request;

import com.doan2025.webtoeic.dto.response.QuestionResponse;
import com.doan2025.webtoeic.dto.response.UserResponse;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BankRequest {
    private Long id;
    private String url;
    private String questionBankTitle;
    private List<QuestionResponse> questions;
    private UserResponse createdBy;
    private UserResponse updatedBy;
    private Boolean isActive;
    private Boolean isDeleted;
    private Date createdAt;
    private Date updatedAt;
}
