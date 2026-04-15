package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.dto.response.CategoryResponse;
import com.doan2025.webtoeic.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/type-class-notification")
    public ApiResponse<List<CategoryResponse>> getTypeClassNotification() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getTypeClassNotification());
    }

    @GetMapping("/join-class-status")
    public ApiResponse<List<CategoryResponse>> getJoinClassStatus() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getJoinClassStatus());
    }

    @GetMapping("/status-attendance")
    public ApiResponse<List<CategoryResponse>> getStatusAttendance() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getStatusAttendance());
    }


    @GetMapping("/status-class")
    public ApiResponse<List<CategoryResponse>> getStatusClass() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getStatusClass());
    }

    @GetMapping("/status-schedule")
    public ApiResponse<List<CategoryResponse>> getStatusSchedule() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getStatusSchedule());
    }

    @GetMapping("/status-order")
    public ApiResponse<List<CategoryResponse>> getStatusOrder() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getStatusOrder());
    }

    @GetMapping("/post")
    public ApiResponse<List<CategoryResponse>> getPosts() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getCategoryPost());
    }

    @GetMapping("/gender")
    public ApiResponse<List<CategoryResponse>> getGenders() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getCategoryGender());
    }

    @GetMapping("/role")
    public ApiResponse<List<CategoryResponse>> getRoles() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getCategoryRole());
    }

    @GetMapping("/course")
    public ApiResponse<List<CategoryResponse>> getCourses() {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.CATEGORY, categoryService.getCategoryCourse());
    }


}
