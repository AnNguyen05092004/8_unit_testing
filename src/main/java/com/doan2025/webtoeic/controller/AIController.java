package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {
    private final AIService aiService;

    @GetMapping
    public String getAI() {
        return aiService.checkCallAI();
    }

    @GetMapping("/analysis-question")
    public ApiResponse<?> analysisQuestion(@RequestParam("url") String url) {
        return ApiResponse.of(ResponseCode.ANALYSIS_SUCCESS, ResponseObject.QUESTION, aiService.analysisWithAI(url));
    }
}
