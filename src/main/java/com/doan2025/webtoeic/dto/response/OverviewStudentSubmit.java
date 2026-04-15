package com.doan2025.webtoeic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class OverviewStudentSubmit {
    private UserResponse userResponse;
    private List<StudentQuizResponse> quizSubmit;
}
