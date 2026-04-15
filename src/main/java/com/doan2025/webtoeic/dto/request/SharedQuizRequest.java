package com.doan2025.webtoeic.dto.request;


import lombok.Data;

import java.util.Date;

@Data
public class SharedQuizRequest {
    private Long sharedQuizId;
    private Long classId;
    private Long quizId;
    private Date startAt;
    private Date endAt;
    private Boolean isActive;
    private Boolean isDelete;
}
