package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.LessonRequest;
import com.doan2025.webtoeic.dto.response.LessonResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LessonService {
    LessonResponse getDetail(HttpServletRequest httpServletRequest, Long id);

    Page<LessonResponse> getLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    Page<LessonResponse> getOwnLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    Page<LessonResponse> getAllLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    LessonResponse disableOrDelete(HttpServletRequest request, LessonRequest lesson);

    LessonResponse updateLesson(HttpServletRequest request, LessonRequest lesson);

    LessonResponse createLesson(HttpServletRequest request, LessonRequest lesson);
}
