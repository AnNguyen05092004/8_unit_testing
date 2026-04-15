package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.request.ExplanationQuestionRequest;
import com.doan2025.webtoeic.dto.response.ExplanationQuestionResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ExplanationQuestionService {
    ExplanationQuestionResponse getExplanationQuestion(HttpServletRequest httpServletRequest, Long id);

    List<ExplanationQuestionResponse> getExplanationQuestions(HttpServletRequest httpServletRequest, Long id);

    ExplanationQuestionResponse saveExplanationQuestion(HttpServletRequest httpServletRequest, ExplanationQuestionRequest explanationQuestion);

    ExplanationQuestionResponse updateExplanationQuestion(HttpServletRequest httpServletRequest, ExplanationQuestionRequest explanationQuestion);

    void deleteExplanationQuestion(HttpServletRequest httpServletRequest, Long id);
}
