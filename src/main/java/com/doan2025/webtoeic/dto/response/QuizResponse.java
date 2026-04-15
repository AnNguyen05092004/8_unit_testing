package com.doan2025.webtoeic.dto.response;

import com.doan2025.webtoeic.constants.enums.EQuizStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizResponse {

    private Long id;

    private String title;

    private String description;

    private Long totalQuestions;

    private EQuizStatus status;

    private List<QuestionResponse> questions;

    private Date createAt;

    private Date updateAt;

    private UserResponse createBy;

    private UserResponse updateBy;
}
