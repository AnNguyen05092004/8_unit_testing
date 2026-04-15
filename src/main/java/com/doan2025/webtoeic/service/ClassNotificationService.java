package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchNotificationInClassDto;
import com.doan2025.webtoeic.dto.request.ClassNotificationRequest;
import com.doan2025.webtoeic.dto.response.ClassNotificationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassNotificationService {

    ClassNotificationResponse getDetailNotificationInClass(HttpServletRequest httpServletRequest, Long notificationId);

    Page<ClassNotificationResponse> getListNotificationInClass(HttpServletRequest httpServletRequest, SearchNotificationInClassDto dto, Pageable pageable);

    ClassNotificationResponse createNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request);

    ClassNotificationResponse updateNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request);

    ClassNotificationResponse disableOrDeleteNotificationInClass(HttpServletRequest httpServletRequest, ClassNotificationRequest request);
}
