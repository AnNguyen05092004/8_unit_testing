package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.NotiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/noti")
public class NotiController {
    private final NotiService notiService;

    @GetMapping("/count")
    public ApiResponse<Long> countNoti(HttpServletRequest request) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.NOTIFICATION,
                notiService.countNoti(request));
    }

    @GetMapping("/list")
    public ApiResponse<Page<?>> filter(HttpServletRequest request, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.NOTIFICATION,
                notiService.listNoti(request, pageable));
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(HttpServletRequest request, @RequestParam List<Long> notiIds) {
        notiService.updateNoti(request, notiIds);
        return ApiResponse.of(ResponseCode.GET_SUCCESS,
                ResponseObject.NOTIFICATION,
                null);
    }
}
