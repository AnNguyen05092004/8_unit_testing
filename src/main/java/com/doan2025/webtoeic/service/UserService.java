package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> getListUserFilter(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);
    UserResponse getUserCurrent(HttpServletRequest request);
    UserResponse getUserDetails(UserRequest request);
    UserResponse updateUserDetails(HttpServletRequest request,UserRequest userRequest);
    UserResponse deleteOrDisableUser(UserRequest request);
    void resetPassword(UserRequest request);
}
