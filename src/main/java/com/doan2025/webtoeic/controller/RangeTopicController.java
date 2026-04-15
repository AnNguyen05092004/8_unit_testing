package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchRangeTopicAndScoreScaleDto;
import com.doan2025.webtoeic.dto.request.RangeTopicRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.RangeTopicService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/range-topic")
public class RangeTopicController {
    private final RangeTopicService rangeTopicService;

    @GetMapping()
    public ApiResponse<?> getRangeTopic(HttpServletRequest request, Long id) {
        return ApiResponse.of(
                ResponseCode.GET_SUCCESS,
                ResponseObject.RANGE_TOPIC,
                rangeTopicService.getRangeTopic(request, id)
        );
    }

    @PostMapping("/filter")
    public ApiResponse<?> filter(HttpServletRequest request, @RequestBody SearchRangeTopicAndScoreScaleDto dto, Pageable pageable) {
        return ApiResponse.of(
                ResponseCode.GET_SUCCESS,
                ResponseObject.RANGE_TOPIC,
                rangeTopicService.getRangeTopics(request, dto, pageable)
        );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> createRangeTopic(HttpServletRequest request, @RequestBody RangeTopicRequest dto) {
        return ApiResponse.of(
                ResponseCode.CREATE_SUCCESS,
                ResponseObject.RANGE_TOPIC,
                rangeTopicService.createRangeTopic(request, dto)
        );
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> updateRangeTopic(HttpServletRequest request, @RequestBody RangeTopicRequest dto) {
        return ApiResponse.of(
                ResponseCode.UPDATE_SUCCESS,
                ResponseObject.RANGE_TOPIC,
                rangeTopicService.updateRangeTopic(dto, request)
        );
    }
}
