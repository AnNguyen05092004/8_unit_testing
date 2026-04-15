package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import com.doan2025.webtoeic.dto.SearchSubmittedDto;
import com.doan2025.webtoeic.dto.request.QuizRequest;
import com.doan2025.webtoeic.dto.request.SharedQuizRequest;
import com.doan2025.webtoeic.dto.request.SubmitRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.QuizService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/quiz")
public class QuizController {
    private final QuizService quizzService;

    @PostMapping("statistic-detail-quiz-in-class")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> statisticDetailQuizInClass(HttpServletRequest httpServletRequest,
                                                     @RequestParam("id-quiz") Long idQuiz,
                                                     @RequestParam("id-class") Long idClass,
                                                     @RequestParam("score") Long score,
                                                     @RequestBody SearchSubmittedDto dto) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.statisticDetailQuizInClass(httpServletRequest, idQuiz, idClass, score, dto));
    }

    @PostMapping("statistic-overview-quizzes-in-class")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> statisticOverviewQuizInClass(HttpServletRequest httpServletRequest,
                                                       @RequestParam("id-class") Long idClass,
                                                       @RequestParam("score") Long score,
                                                       @RequestBody SearchQuizDto dto) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.statisticOverviewQuizInClass(httpServletRequest, idClass, score, dto));
    }

    // get detail submitted
    @PostMapping("view-detail-submitted-quiz-by-student-in-class")
    public ApiResponse<?> viewSubmittedQuizInClass(HttpServletRequest httpServletRequest,
                                                   @RequestParam("id-submitted") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.QUIZ, quizzService.getDetailSubmitQuiz(httpServletRequest, id));
    }

    // search list submitted
    @PostMapping("search-submitted-quiz-by-student-in-class")
    public ApiResponse<Page<?>> searchSubmittedQuizInClass(HttpServletRequest httpServletRequest,
                                                           @RequestParam("id-quiz") Long idQuiz,
                                                           @RequestParam("id-class") Long idClass,
                                                           @RequestBody SearchSubmittedDto dto,
                                                           Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.getListSubmitQuiz(httpServletRequest, idQuiz, idClass, dto, pageable));
    }

    @GetMapping("overview-student-submit-in-class")
    public ApiResponse<Page<?>> overviewStudentSubmitInClass(HttpServletRequest httpServletRequest,
                                                             @RequestParam("id-class") Long idClass,
                                                             Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.overviewStudentSubmitInClass(httpServletRequest, idClass, pageable));
    }

    @PostMapping("submit-quiz-in-class")
    public ApiResponse<Void> submitQuizInClass(HttpServletRequest httpServletRequest,
                                               @RequestParam("id-quiz") Long idQuiz,
                                               @RequestParam("id-class") Long idClass,
                                               @RequestParam("des") @Nullable String des,
                                               @RequestBody(required = false) List<SubmitRequest> requests) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        quizzService.submitQuiz(httpServletRequest, idQuiz, requests, idClass, des);
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS, ResponseObject.SUBMIT, null);
    }

    @PostMapping("list-quiz-in-class")
    public ApiResponse<?> listQuizInClass(HttpServletRequest httpServletRequest,
                                          @RequestParam("id-class") Long idClass,
                                          @RequestBody SearchQuizDto searchQuizDto,
                                          Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.getListQuizInClass(httpServletRequest, idClass, searchQuizDto, pageable));
    }

    @PostMapping("update-quiz-in-class")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<Void> updateTimeQuizInClass(HttpServletRequest httpServletRequest, @RequestBody SharedQuizRequest request) {
        quizzService.updateQuizInClass(httpServletRequest, request);
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.CLASS, null);
    }


    @PostMapping("pull-quiz-to-class")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<Void> pullQuizToClass(HttpServletRequest httpServletRequest, @RequestBody SharedQuizRequest request) {
        quizzService.pullQuizToClass(httpServletRequest, request);
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.CLASS, null);
    }

    @GetMapping
    public ApiResponse<?> getDetailQuizz(HttpServletRequest httpServletRequest, @RequestParam("id") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.getQuiz(httpServletRequest, id));
    }

    @PostMapping("search-quizz")
    public ApiResponse<?> searchQuizz(HttpServletRequest httpServletRequest, @RequestBody SearchQuizDto dto, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.getQuizes(httpServletRequest, dto, pageable));
    }

    @PostMapping("create-quizz")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> createQuizz(HttpServletRequest httpServletRequest, @RequestBody QuizRequest quizRequest) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.createQuiz(httpServletRequest, quizRequest));
    }

    @PostMapping("update-quiz")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> updateQuizz(HttpServletRequest httpServletRequest, @RequestBody QuizRequest quizRequest) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.updateQuiz(httpServletRequest, quizRequest));
    }

    @GetMapping("convert-bank-to-quiz")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> convertBankToQuiz(HttpServletRequest httpServletRequest, Long idBank) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.convertBankToQuiz(httpServletRequest, idBank));
    }

    @PostMapping("add-questions-to-quiz")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> addQuestionsToQuiz(HttpServletRequest httpServletRequest, @RequestBody QuizRequest quizRequest) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.addQuestionToQuiz(httpServletRequest, quizRequest));
    }

    @PostMapping("remove-questions-from-quiz")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> removeQuestionsToQuiz(HttpServletRequest httpServletRequest, @RequestBody QuizRequest quizRequest) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUIZ,
                quizzService.removeQuestionFromQuiz(httpServletRequest, quizRequest));
    }
}
