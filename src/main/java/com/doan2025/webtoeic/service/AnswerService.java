package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.request.AnswerRequest;
import com.doan2025.webtoeic.dto.response.AnswerResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AnswerService {
    AnswerResponse getAnswer(HttpServletRequest httpServletRequest, Long id);

    AnswerResponse getAnswerList(HttpServletRequest httpServletRequest, Long idQuestion);

    AnswerResponse saveAnswer(HttpServletRequest httpServletRequest, AnswerRequest answerRequest);

    AnswerResponse updateAnswer(HttpServletRequest httpServletRequest, AnswerRequest answerRequest);

    AnswerResponse deleteAnswer(HttpServletRequest httpServletRequest, Long id);
}
