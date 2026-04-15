package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassService {

    ClassResponse get(HttpServletRequest httpServletRequest, Long classId);

    Page<ClassResponse> getClasses(HttpServletRequest httpServletRequest, SearchClassDto dto, Pageable pageable);

    void deleteClass(List<Long> ids, HttpServletRequest httpServletRequest);

    ClassResponse updateClass(ClassRequest classRequest, HttpServletRequest httpServletRequest);

    ClassResponse createClass(HttpServletRequest request, ClassRequest classRequest);
}
