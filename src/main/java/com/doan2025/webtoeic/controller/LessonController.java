package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.LessonRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.service.LessonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lesson")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;

    @GetMapping()
    public ApiResponse<?> getLessonDetail(HttpServletRequest request, @RequestParam("id") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.LESSON, lessonService.getDetail(request, id));
    }

    @PostMapping("/get-lessons")
    public ApiResponse<?> getLessons(HttpServletRequest request, @RequestBody SearchBaseDto dto,
                                     Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.LESSON, lessonService.getLessons(request, dto, pageable));
    }

    @PostMapping("/get-own-lessons")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> getOwnLesson(HttpServletRequest request, @RequestBody SearchBaseDto dto,
                                       Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.LESSON, lessonService.getOwnLessons(request, dto, pageable));
    }

    @PostMapping("/get-all-lessons")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> getAllLesson(HttpServletRequest request, @RequestBody SearchBaseDto dto,
                                       Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.LESSON, lessonService.getAllLessons(request, dto, pageable));
    }

    @PostMapping("/update-status")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> disableOrDeleteLesson(HttpServletRequest request, @RequestBody LessonRequest lesson) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.LESSON, lessonService.disableOrDelete(request, lesson));
    }

    @PostMapping("/update-info")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> updateLesson(HttpServletRequest request, @RequestBody LessonRequest lesson) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.LESSON, lessonService.updateLesson(request, lesson));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> createLesson(HttpServletRequest request, @RequestBody LessonRequest lesson) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS, ResponseObject.LESSON, lessonService.createLesson(request, lesson));
    }
}
