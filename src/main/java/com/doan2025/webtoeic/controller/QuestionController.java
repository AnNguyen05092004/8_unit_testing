package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchQuestionDto;
import com.doan2025.webtoeic.dto.request.QuestionRequest;
import com.doan2025.webtoeic.dto.response.AiResponse;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/question")
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("detail")
    public ApiResponse<?> detail(HttpServletRequest httpServletRequest, Long questionId) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.QUESTION,
                questionService.getDetail(httpServletRequest, questionId));
    }

    @PostMapping("add-question-to-bank")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> addQuestionToBank(HttpServletRequest httpServletRequest, @RequestParam("bankId") Long bankId, @RequestBody QuestionRequest request) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS,
                ResponseObject.BANK,
                questionService.addQuestionToBank(httpServletRequest, request, bankId));
    }

    @PostMapping("remove-question-from-bank")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> removeQuestionFromBank(HttpServletRequest httpServletRequest,
                                                 @RequestParam("bankId") Long bankId,
                                                 @RequestParam("questionsIds") List<Long> questionIds) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS,
                ResponseObject.BANK,
                questionService.removeQuestionFromBank(httpServletRequest, questionIds, bankId));
    }

    @PostMapping("filter-question")
    public ApiResponse<?> filterQuestion(HttpServletRequest httpServletRequest,
                                         @RequestBody SearchQuestionDto dto,
                                         Pageable pageable) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUESTION,
                questionService.getQuestionList(httpServletRequest, dto, pageable));
    }

    @PostMapping("create-for-ai")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> createForAi(HttpServletRequest httpServletRequest, @RequestBody AiResponse request) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.QUESTION,
                questionService.saveQuestion(httpServletRequest, request));
    }

    @PostMapping("update")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> update(HttpServletRequest httpServletRequest, @RequestBody QuestionRequest request) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS,
                ResponseObject.QUESTION,
                questionService.updateQuestion(httpServletRequest, request));
    }
}
