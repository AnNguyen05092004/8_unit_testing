package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.*;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.CourseRequest;
import com.doan2025.webtoeic.dto.response.CourseResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.CourseService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = {WebToeicException.class, Exception.class})
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ConvertUtil convertUtil;
    private final EnrollmentRepository enrollmentRepository;
    private final NotiUtils notiUtils;

    @Override
    public Page<CourseResponse> findByCourseBought(HttpServletRequest httpServletRequest, Pageable pageable) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Page<Course> courses = enrollmentRepository.findCourseByUser(user, pageable);
        List<CourseResponse> courseResponses = courses.getContent().stream()
                .map(item -> convertUtil.convertCourseToDto(httpServletRequest, item))
                .collect(Collectors.toList());
        return new PageImpl<>(courseResponses, pageable, courses.getTotalElements());
    }

    @Override
    public CourseResponse getCourseDetail(HttpServletRequest httpServletRequest, Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));
        return convertUtil.convertCourseToDto(httpServletRequest, course);
    }

    @Override
    public Page<CourseResponse> getCourses(HttpServletRequest httpServletRequest, SearchBaseDto dto, Pageable pageable) {
        String email = "";
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(httpServletRequest);
        }
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        return courseRepository.findCourses(dto, email, pageable);
    }

    @Override
    public Page<CourseResponse> getAllCourses(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        return courseRepository.findAllCourses(dto, pageable);
    }

    @Override
    public Page<CourseResponse> getOwnCourses(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        String email = jwtUtil.getEmailFromToken(request);
        dto.setEmail(email);
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        return courseRepository.findOwnCourses(dto, email, pageable);
    }

    @Override
    public CourseResponse createCourse(HttpServletRequest httpServletRequest, CourseRequest request) {
        if (request.getCategoryId() == null) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.CATEGORY);
        }
        if (request.getAuthorId() == null) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.USER);
        }
        if (request.getTitle() == null || request.getTitle().isEmpty()) {
            throw new WebToeicException(ResponseCode.IS_NULL, ResponseObject.TITLE);
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new WebToeicException(ResponseCode.NOT_AVAILABLE, ResponseObject.PRICE);
        }
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        ECategoryCourse categoryCourse = ECategoryCourse.fromValue(request.getCategoryId());
        User createdBy = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Course course = Course.builder()
                .author(author)
                .categoryCourse(categoryCourse)
                .price(request.getPrice())
                .description(request.getDescription())
                .title(request.getTitle())
                .createdBy(createdBy)
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
        Course savedCourse = courseRepository.save(course);
        List<User> users = userRepository.findUserOnlyStudent();
        notiUtils.sendNoti(users,
                ENotiType.NEW_COURSE,
                Constants.NEW_COURSE_CONTENT,
                Constants.NEW_COURSE_CONTENT,
                course.getId());
        return convertUtil.convertCourseToDto(httpServletRequest, savedCourse);
    }

    @Override
    public CourseResponse updateCourse(HttpServletRequest httpServletRequest, CourseRequest request) {
        User updatedBy = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Course course = courseRepository.findById(request.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));
        List.of(
                new FieldUpdateUtil<>(course::getTitle, course::setTitle, request.getTitle()),
                new FieldUpdateUtil<>(course::getDescription, course::setDescription, request.getDescription()),
                new FieldUpdateUtil<>(course::getPrice, course::setPrice, request.getPrice()),
                new FieldUpdateUtil<>(course::getThumbnailUrl, course::setThumbnailUrl, request.getThumbnailUrl()),
                new FieldUpdateUtil<>(course::getAuthor, course::setAuthor,
                        userRepository.findById(request.getAuthorId())
                                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))),
                new FieldUpdateUtil<>(course::getThumbnailUrl, course::setThumbnailUrl, request.getThumbnailUrl()),
                new FieldUpdateUtil<>(course::getCategoryCourse, course::setCategoryCourse, ECategoryCourse.fromValue(request.getCategoryId()))
        ).forEach(FieldUpdateUtil::updateIfNeeded);
        course.setUpdatedBy(updatedBy);
        return convertUtil.convertCourseToDto(httpServletRequest, courseRepository.save(course));
    }

    @Override
    public CourseResponse disableOrDeleteCourse(HttpServletRequest httpServletRequest, CourseRequest request) {
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(httpServletRequest))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Course course = courseRepository.findById(request.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));
        if (user.getRole().equals(ERole.MANAGER)) {
            // function: disable course
            if (request.getIsActive() != null && !request.getIsActive().equals(course.getIsActive())) {
                course.setIsActive(request.getIsActive());
            }
            // function: delete course
            if (request.getIsDelete() != null && !course.getIsDelete().equals(request.getIsDelete())) {
                course.setIsDelete(request.getIsDelete());
            }
            return convertUtil.convertCourseToDto(httpServletRequest, courseRepository.save(course));
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);
    }
}
