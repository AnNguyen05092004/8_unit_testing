package com.doan2025.webtoeic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuizResponse {
    private Long idQuiz;
    private String titleQuiz;
    private Date startAt;
    private Date endAt;
    private BigDecimal score;
    private String des;
}
