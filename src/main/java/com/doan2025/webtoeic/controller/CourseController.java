package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.CourseRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.dto.response.CourseResponse;
import com.doan2025.webtoeic.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public ApiResponse<CourseResponse> getCourse(HttpServletRequest request, @RequestParam("id") Long id) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.COURSE, courseService.getCourseDetail(request, id));
    }

    @GetMapping("/my-bought-course")
    public ApiResponse<Page<CourseResponse>> getBoughtCourse(HttpServletRequest request, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.COURSE, courseService.findByCourseBought(request, pageable));
    }

    @PostMapping("/get-courses")
    public ApiResponse<?> getCourses(HttpServletRequest request, @RequestBody SearchBaseDto dto, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.COURSE, courseService.getCourses(request, dto, pageable));
    }

    @PostMapping("/all-courses")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<?> getAllCourses(HttpServletRequest request, @RequestBody SearchBaseDto dto, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.COURSE, courseService.getAllCourses(request, dto, pageable));
    }

    @PostMapping("/own-courses")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> getOwnCourses(HttpServletRequest request, @RequestBody SearchBaseDto dto, Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.COURSE, courseService.getOwnCourses(request, dto, pageable));
    }

    @PostMapping("/update-status")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> updateStatusCourse(HttpServletRequest request, @RequestBody CourseRequest course) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.COURSE, courseService.disableOrDeleteCourse(request, course));
    }

    @PostMapping("/update-info")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> updateInfoCourse(HttpServletRequest request, @RequestBody CourseRequest course) {
        return ApiResponse.of(ResponseCode.UPDATE_SUCCESS, ResponseObject.COURSE, courseService.updateCourse(request, course));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<?> createCourse(HttpServletRequest request, @RequestBody CourseRequest course) {
        return ApiResponse.of(ResponseCode.CREATE_SUCCESS, ResponseObject.COURSE, courseService.createCourse(request, course));
    }

}
