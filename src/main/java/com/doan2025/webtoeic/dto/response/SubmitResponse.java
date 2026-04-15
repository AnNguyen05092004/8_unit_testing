package com.doan2025.webtoeic.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResponse {
    private Long idSubmitted;
    private String titleQuiz;
    private QuizResponse quiz;
    private UserResponse user;
    private BigDecimal score;
    private Date startAt;
    private Date endAt;
    private String des;
}
