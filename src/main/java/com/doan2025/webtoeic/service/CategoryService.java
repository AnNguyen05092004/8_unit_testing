package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getJoinClassStatus();

    List<CategoryResponse> getTypeClassNotification();

    List<CategoryResponse> getStatusAttendance();

    List<CategoryResponse> getStatusClass();

    List<CategoryResponse> getStatusSchedule();

    List<CategoryResponse> getStatusOrder();

    List<CategoryResponse> getCategoryPost();

    List<CategoryResponse> getCategoryGender();

    List<CategoryResponse> getCategoryRole();

    List<CategoryResponse> getCategoryCourse();
}
