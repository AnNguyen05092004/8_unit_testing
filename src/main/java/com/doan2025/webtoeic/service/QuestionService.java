package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchQuestionDto;
import com.doan2025.webtoeic.dto.request.QuestionRequest;
import com.doan2025.webtoeic.dto.response.AiResponse;
import com.doan2025.webtoeic.dto.response.BankResponse;
import com.doan2025.webtoeic.dto.response.QuestionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuestionService {
    QuestionResponse getDetail(HttpServletRequest httpServletRequest, Long id);

    Page<QuestionResponse> getQuestionList(HttpServletRequest httpServletRequest, SearchQuestionDto dto, Pageable pageable);

    BankResponse saveQuestion(HttpServletRequest httpServletRequest, AiResponse aiResponse);

    BankResponse addQuestionToBank(HttpServletRequest httpServletRequest, QuestionRequest questionRequest, Long bankId);

    BankResponse removeQuestionFromBank(HttpServletRequest httpServletRequest, List<Long> questionIds, Long bankId);

    QuestionResponse updateQuestion(HttpServletRequest httpServletRequest, QuestionRequest questionRequest);

}
