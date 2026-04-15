package com.doan2025.webtoeic.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class SubmitRequest {
    private Long questionId;
    private Long answerId;
    private Date startAt;
    private Date endAt;
}
