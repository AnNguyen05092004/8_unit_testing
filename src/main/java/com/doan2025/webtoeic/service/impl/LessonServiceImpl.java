package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.AttachDocumentLesson;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.Lesson;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.LessonRequest;
import com.doan2025.webtoeic.dto.response.AttachDocumentLessonResponse;
import com.doan2025.webtoeic.dto.response.LessonResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttachDocumentLessonRepository;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.LessonRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.CloudService;
import com.doan2025.webtoeic.service.LessonService;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.FieldUpdateUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, WebToeicException.class})
public class LessonServiceImpl implements LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final CloudService cloudService;
    private final AttachDocumentLessonRepository attachDocumentLessonRepository;
    private final ConvertUtil convertUtil;


    @Override
    public LessonResponse getDetail(HttpServletRequest httpServletRequest, Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.LESSON));
        return convertUtil.convertLessonToDto(httpServletRequest,
                lesson,
                attachDocumentLessonRepository.findAllByLessonId(lesson.getId()));
    }

    @Override
    public Page<LessonResponse> getLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        String email = "";
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            email = jwtUtil.getEmailFromToken(request);
        }
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        Page<LessonResponse> responses = lessonRepository.findLessons(dto, email, pageable);
        List<LessonResponse> lessonResponses = responses.getContent();
        for (LessonResponse item : lessonResponses) {
            List<AttachDocumentLesson> attachDocumentLessons = attachDocumentLessonRepository.findAllByLessonId(item.getId());
            List<AttachDocumentLessonResponse> attachDocumentLessonResponses = attachDocumentLessons.stream()
                    .map(doc -> AttachDocumentLessonResponse.builder()
                            .id(doc.getId())
                            .linkUrl(doc.getLinkUrl())
                            .isActive(doc.getIsActive())
                            .isDelete(doc.getIsDelete())
                            .createdAt(doc.getCreatedAt())
                            .updatedAt(doc.getUpdatedAt())
                            .build())
                    .toList();
            item.setAttachDocumentLessons(attachDocumentLessonResponses);
        }
        return new PageImpl<>(lessonResponses, pageable, responses.getTotalElements());
    }

    @Override
    public Page<LessonResponse> getOwnLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        String email = jwtUtil.getEmailFromToken(request);
        return lessonRepository.findOwnLessons(dto, email, pageable);
    }

    @Override
    public Page<LessonResponse> getAllLessons(HttpServletRequest request, SearchBaseDto dto, Pageable pageable) {
        if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
            dto.setCategories(null);
        }
        return lessonRepository.findAllLessons(dto, pageable);
    }

    @Override
    public LessonResponse disableOrDelete(HttpServletRequest request, LessonRequest lessonRequest) {
        String email = jwtUtil.getEmailFromToken(request);

        User updatedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Lesson lesson = lessonRepository.findById(lessonRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.LESSON));

        if ((updatedBy.getRole().equals(ERole.CONSULTANT)
                && lesson.getCreatedBy().getEmail().equals(email))
                || updatedBy.getRole().equals(ERole.MANAGER)) {
            // function: disable
            if (lessonRequest.getIsActive() != null && !lesson.getIsActive().equals(lessonRequest.getIsActive())) {
                lesson.setIsActive(lessonRequest.getIsActive());
            }
            // function: delete
            if (lessonRequest.getIsDelete() != null && !lesson.getIsDelete().equals(lessonRequest.getIsDelete())) {
                lesson.setIsDelete(lessonRequest.getIsDelete());
            }
            Lesson savedLesson = lessonRepository.save(lesson);
            return convertUtil.convertLessonToDto(request,
                    savedLesson,
                    attachDocumentLessonRepository.findAllByLessonId(savedLesson.getId()));
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.LESSON);
    }

    @Override
    public LessonResponse updateLesson(HttpServletRequest request, LessonRequest lessonRequest) {
        String email = jwtUtil.getEmailFromToken(request);

        User updatedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));

        Lesson lesson = lessonRepository.findById(lessonRequest.getId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.LESSON));
        if ((updatedBy.getRole().equals(ERole.CONSULTANT)
                && lesson.getCreatedBy().getEmail().equals(email))
                || updatedBy.getRole().equals(ERole.MANAGER)) {
            List.of(
                    new FieldUpdateUtil<>(lesson::getTitle, lesson::setTitle, lessonRequest.getTitle()),
                    new FieldUpdateUtil<>(lesson::getContent, lesson::setContent, lessonRequest.getContent()),
                    new FieldUpdateUtil<>(lesson::getVideoUrl, lesson::setVideoUrl, lessonRequest.getVideoUrl()),
                    new FieldUpdateUtil<>(lesson::getIsPreviewAble, lesson::setIsPreviewAble, lessonRequest.getIsPreviewAble()),
                    new FieldUpdateUtil<>(lesson::getOrderIndex, lesson::setOrderIndex, lessonRequest.getOrderIndex())
            ).forEach(FieldUpdateUtil::updateIfNeeded);

            if (Objects.nonNull(lessonRequest.getDocumentUrls())) {
                attachDocumentLessonRepository.deleteAttachDocumentLessonsByLesson_Id(lesson.getId());

                for (String documentUrl : lessonRequest.getDocumentUrls()) {
                    AttachDocumentLesson doc = AttachDocumentLesson.builder()
                            .lesson(lesson)
                            .linkUrl(documentUrl)
                            .build();
                    attachDocumentLessonRepository.save(doc);
                }
            }
            Lesson saved = lessonRepository.save(lesson);
            return convertUtil.convertLessonToDto(request,
                    saved,
                    attachDocumentLessonRepository.findAllByLessonId(saved.getId()));
        }
        throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER);

    }

    @Override
    public LessonResponse createLesson(HttpServletRequest request, LessonRequest lesson) {
        User createdBy = userRepository.findByEmail(jwtUtil.getEmailFromToken(request))
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER));
        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE));
        if (lesson.getContent() == null || lesson.getContent().trim().isEmpty()) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CONTENT);
        }
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.TITLE);
        }
        if (lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
            throw new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.URL);
        }

//        Lesson saveLesson = modelMapper.map(lesson, Lesson.class);
        Lesson saveLesson = Lesson.builder()
                .course(course)
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .isPreviewAble(lesson.getIsPreviewAble())
                .orderIndex(course.getLessons().size() + 1)
                .duration(lesson.getDuration())
                .videoUrl(lesson.getVideoUrl())
                .build();
//        saveLesson.setCourse(course);
//        saveLesson.setOrderIndex(course.getLessons().size() + 1);
        saveLesson.setCreatedBy(createdBy);
//        Double duration = cloudService.getVideoDuration(lesson.getVideoUrl());
//        saveLesson.setDuration(duration);
        Lesson savedLesson = lessonRepository.save(saveLesson);
        if (Objects.nonNull(lesson.getDocumentUrls())) {
            for (String documentUrl : lesson.getDocumentUrls()) {
                AttachDocumentLesson document = AttachDocumentLesson.builder()
                        .lesson(saveLesson)
                        .linkUrl(documentUrl)
                        .build();
                attachDocumentLessonRepository.save(document);
            }
        }

        return convertUtil.convertLessonToDto(request,
                savedLesson,
                attachDocumentLessonRepository.findAllByLessonId(savedLesson.getId()));
    }
}
