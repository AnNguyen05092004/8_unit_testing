package com.doan2025.webtoeic.service;

import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.CourseRequest;
import com.doan2025.webtoeic.dto.response.CourseResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    Page<CourseResponse> findByCourseBought(HttpServletRequest httpServletRequest, Pageable pageable);

    CourseResponse getCourseDetail(HttpServletRequest httpServletRequest, Long id);

    Page<CourseResponse> getCourses(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    Page<CourseResponse> getAllCourses(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    Page<CourseResponse> getOwnCourses(HttpServletRequest request, SearchBaseDto dto, Pageable pageable);

    CourseResponse createCourse(HttpServletRequest httpServletRequest, CourseRequest request);

    CourseResponse updateCourse(HttpServletRequest httpServletRequest, CourseRequest request);

    CourseResponse disableOrDeleteCourse(HttpServletRequest httpServletRequest, CourseRequest request);
}
