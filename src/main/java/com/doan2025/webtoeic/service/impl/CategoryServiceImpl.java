package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.dto.response.CategoryResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, WebToeicException.class})
public class CategoryServiceImpl implements CategoryService {

    private <E extends Enum<E>> List<CategoryResponse> mapEnumToCategory(
            E[] values,
            Function<E, Integer> idMapper,
            Function<E, String> nameMapper
    ) {
        return Arrays.stream(values)
                .map(e -> new CategoryResponse(idMapper.apply(e), nameMapper.apply(e).toUpperCase()))
                .collect(Collectors.toList());
    }


    @Override
    public List<CategoryResponse> getJoinClassStatus() {
        return List.of();
    }

    @Override
    public List<CategoryResponse> getTypeClassNotification() {
        return mapEnumToCategory(EClassNotificationType.values(), EClassNotificationType::getValue, EClassNotificationType::getName);
    }

    @Override
    public List<CategoryResponse> getStatusAttendance() {
        return mapEnumToCategory(EAttendanceStatus.values(), EAttendanceStatus::getValue, EAttendanceStatus::getName);
    }

    @Override
    public List<CategoryResponse> getStatusClass() {
        return mapEnumToCategory(EClassStatus.values(), EClassStatus::getValue, EClassStatus::getName);
    }

    @Override
    public List<CategoryResponse> getStatusSchedule() {
        return mapEnumToCategory(EScheduleStatus.values(), EScheduleStatus::getValue, EScheduleStatus::getName);
    }

    @Override
    public List<CategoryResponse> getStatusOrder() {
        return mapEnumToCategory(EStatusOrder.values(), EStatusOrder::getValue, EStatusOrder::getName);
    }

    @Override
    public List<CategoryResponse> getCategoryPost() {
        return mapEnumToCategory(ECategoryPost.values(), ECategoryPost::getValue, ECategoryPost::getName);
    }

    @Override
    public List<CategoryResponse> getCategoryGender() {
        return mapEnumToCategory(EGender.values(), EGender::getValue, EGender::getName);
    }

    @Override
    public List<CategoryResponse> getCategoryRole() {
        return mapEnumToCategory(ERole.values(), ERole::getValue, ERole::getCode);
    }

    @Override
    public List<CategoryResponse> getCategoryCourse() {
        return mapEnumToCategory(ECategoryCourse.values(), ECategoryCourse::getValue, ECategoryCourse::getName);
    }
}
