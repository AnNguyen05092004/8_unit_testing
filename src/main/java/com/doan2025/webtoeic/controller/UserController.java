package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.ApiResponse;
import com.doan2025.webtoeic.dto.response.UserResponse;
import com.doan2025.webtoeic.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping()
    @PreAuthorize("hasRole('STUDENT') OR hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<UserResponse> getUserCurrent(HttpServletRequest request) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.USER, userService.getUserCurrent(request));
    }

    @PostMapping("/filter")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER') OR hasRole('TEACHER')")
    public ApiResponse<Page<UserResponse>> getListUserFilter(HttpServletRequest request,
                                                             @RequestBody SearchBaseDto dto,
                                                             Pageable pageable) {
        return ApiResponse.of(ResponseCode.GET_SUCCESS, ResponseObject.USER, userService.getListUserFilter(request, dto, pageable));
    }

    @PostMapping()
    @PreAuthorize("hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<UserResponse> getUserDetails(HttpServletRequest request, @RequestBody UserRequest userRequest) {
        return ApiResponse.of(ResponseCode.SUCCESS, ResponseObject.USER, userService.getUserDetails(userRequest));
    }

    @PostMapping(value = "/update-own-info")
    @PreAuthorize("hasRole('STUDENT') OR hasRole('TEACHER') OR hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<UserResponse> updateUserDetails(HttpServletRequest request, @RequestBody UserRequest userRequest) {
        return ApiResponse.of(ResponseCode.SUCCESS, ResponseObject.USER, userService.updateUserDetails(request, userRequest));
    }

    @PostMapping(value = "/delete-user")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<UserResponse> deleteUser(HttpServletRequest request, @RequestBody UserRequest userRequest) {
        return ApiResponse.of(ResponseCode.SUCCESS, ResponseObject.USER, userService.deleteOrDisableUser(userRequest));
    }

    @PostMapping(value = "/disable-user")
    @PreAuthorize("hasRole('CONSULTANT') OR hasRole('MANAGER')")
    public ApiResponse<UserResponse> disableUser(HttpServletRequest request, @RequestBody UserRequest userRequest) {
        return ApiResponse.of(ResponseCode.SUCCESS, ResponseObject.USER, userService.deleteOrDisableUser(userRequest));
    }

}
