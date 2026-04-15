package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.ReaderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/read")
public class ReaderController {
    private final ReaderService readerService;

    @GetMapping
    public ApiResponse<String> readFile(HttpServletRequest request, @RequestParam("url") String url) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.FILE, readerService.readContentOfFile(url));
    }
}
