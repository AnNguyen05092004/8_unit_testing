package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchBankDto;
import com.doan2025.webtoeic.dto.request.BankRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.QuestionBankService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/question-bank")
public class QuestionBankController {
    private final QuestionBankService questionBankService;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> getQuestionBank(HttpServletRequest httpServletRequest, @RequestParam("id") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.BANK,
                questionBankService.getQuestionBank(httpServletRequest, id));
    }

    @PostMapping("filter")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> getQuestionBankFilter(HttpServletRequest httpServletRequest, @RequestBody SearchBankDto dto, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.BANK,
                questionBankService.getQuestionBanks(httpServletRequest, dto, pageable));
    }

    @PostMapping("create")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> create(HttpServletRequest httpServletRequest, @RequestBody BankRequest request) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.BANK,
                questionBankService.saveQuestionBank(httpServletRequest, request));
    }

    @PostMapping("update")
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> update(HttpServletRequest httpServletRequest, @RequestBody BankRequest request) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS,
                ResponseObject.BANK,
                questionBankService.updateQuestionBank(httpServletRequest, request));
    }

}
