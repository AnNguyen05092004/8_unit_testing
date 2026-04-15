package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchQuizDto;
import com.doan2025.webtoeic.dto.SearchSubmittedDto;
import com.doan2025.webtoeic.dto.request.QuizRequest;
import com.doan2025.webtoeic.dto.request.SharedQuizRequest;
import com.doan2025.webtoeic.dto.request.SubmitRequest;
import com.doan2025.webtoeic.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {
    OverviewResponse statisticDetailQuizInClass(HttpServletRequest httpServletRequest, Long idQuiz, Long idClass, Long score, SearchSubmittedDto dto);

    OverviewResponse statisticOverviewQuizInClass(HttpServletRequest httpServletRequest, Long idClass, Long cscore, SearchQuizDto dto);

    SubmitResponse getDetailSubmitQuiz(HttpServletRequest httpServletRequest, Long idQuiz);

    Page<SubmitResponse> getListSubmitQuiz(HttpServletRequest httpServletRequest, Long iQuiz, Long idClass, SearchSubmittedDto dto, Pageable pageable);

    Page<OverviewStudentSubmit> overviewStudentSubmitInClass(HttpServletRequest httpServletRequest, Long idClass, Pageable pageable);

    void submitQuiz(HttpServletRequest request, Long quizId, List<SubmitRequest> requests, Long idClass, String des);

    Page<ShareQuizResponse> getListQuizInClass(HttpServletRequest httpServletRequest, Long idClass, SearchQuizDto dto, Pageable pageable);

    void updateQuizInClass(HttpServletRequest httpServletRequest, SharedQuizRequest request);

    void pullQuizToClass(HttpServletRequest httpServletRequest, SharedQuizRequest request);

    QuizResponse getQuiz(HttpServletRequest httpServletRequest, Long id);

    Page<QuizResponse> getQuizes(HttpServletRequest httpServletRequest, SearchQuizDto dto, Pageable pageable);

    QuizResponse createQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest);

    QuizResponse convertBankToQuiz(HttpServletRequest httpServletRequest, Long idBank);

    QuizResponse updateQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest);

    QuizResponse addQuestionToQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest);

    QuizResponse removeQuestionFromQuiz(HttpServletRequest httpServletRequest, QuizRequest quizRequest);
}
