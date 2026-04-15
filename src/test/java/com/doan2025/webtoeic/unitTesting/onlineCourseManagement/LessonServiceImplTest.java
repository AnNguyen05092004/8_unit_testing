package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.domain.AttachDocumentLesson;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.Lesson;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.LessonRequest;
import com.doan2025.webtoeic.dto.response.LessonResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttachDocumentLessonRepository;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.LessonRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceImplTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private AttachDocumentLessonRepository attachDocumentLessonRepository;
    @Mock private ConvertUtil convertUtil;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private User managerUser;
    private User consultantUser;
    private User studentUser;
    private Course course;
    private Lesson lesson;
    private LessonResponse lessonResponse;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);

        consultantUser = new User();
        consultantUser.setId(2L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);

        studentUser = new User();
        studentUser.setId(3L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);

        course = new Course();
        course.setId(10L);
        course.setLessons(new ArrayList<>());

        lesson = new Lesson();
        lesson.setId(20L);
        lesson.setTitle("Lesson 1");
        lesson.setContent("Content");
        lesson.setVideoUrl("http://video.url");
        lesson.setIsActive(true);
        lesson.setIsDelete(false);
        lesson.setCreatedBy(consultantUser);
        lesson.setCourse(course);

        lessonResponse = new LessonResponse();
        lessonResponse.setId(20L);
        lessonResponse.setTitle("Lesson 1");
    }

    // TC_LESSON_001
    @Test
    @DisplayName("TC_LESSON_001: getDetail returns lesson response for valid id")
    void getDetailReturnsResponseForValidId() {
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());
        when(convertUtil.convertLessonToDto(eq(request), eq(lesson), any())).thenReturn(lessonResponse);

        LessonResponse result = lessonService.getDetail(request, 20L);

        assertEquals(20L, result.getId());
    }

    // TC_LESSON_002
    @Test
    @DisplayName("TC_LESSON_002: getDetail throws when lesson not found")
    void getDetailThrowsWhenLessonNotFound() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> lessonService.getDetail(request, 99L));
    }

    // TC_LESSON_003
    @Test
    @DisplayName("TC_LESSON_003: getLessons sets categories null when empty list and no Bearer token")
    void getLessonsSetsCategoriesNullAndUsesEmptyEmail() {
        SearchBaseDto dto = new SearchBaseDto();
        dto.setCategories(List.of());
        LessonResponse lr = new LessonResponse();
        lr.setId(20L);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(lessonRepository.findLessons(eq(dto), eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lr)));
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());

        Page<LessonResponse> result = lessonService.getLessons(request, dto, Pageable.unpaged());

        assertNull(dto.getCategories());
        assertEquals(1, result.getTotalElements());
    }

    // TC_LESSON_004
    @Test
    @DisplayName("TC_LESSON_004: getLessons extracts email from Bearer token")
    void getLessonsExtractsEmailFromBearerToken() {
        SearchBaseDto dto = new SearchBaseDto();
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(lessonRepository.findLessons(eq(dto), eq("student@test.com"), any(Pageable.class)))
                .thenReturn(Page.empty());

        lessonService.getLessons(request, dto, Pageable.unpaged());

        verify(lessonRepository).findLessons(eq(dto), eq("student@test.com"), any(Pageable.class));
    }

    // TC_LESSON_005
    @Test
    @DisplayName("TC_LESSON_005: getOwnLessons sets categories null and queries by email")
    void getOwnLessonsSetsEmailAndQueriesOwnLessons() {
        SearchBaseDto dto = new SearchBaseDto();
        dto.setCategories(List.of());
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(lessonRepository.findOwnLessons(eq(dto), eq("consultant@test.com"), any(Pageable.class)))
                .thenReturn(Page.empty());

        lessonService.getOwnLessons(request, dto, Pageable.unpaged());

        assertNull(dto.getCategories());
        verify(lessonRepository).findOwnLessons(eq(dto), eq("consultant@test.com"), any(Pageable.class));
    }

    // TC_LESSON_006
    @Test
    @DisplayName("TC_LESSON_006: getAllLessons sets categories null and delegates to repository")
    void getAllLessonsDelegatesToRepository() {
        SearchBaseDto dto = new SearchBaseDto();
        dto.setCategories(List.of());
        when(lessonRepository.findAllLessons(eq(dto), any(Pageable.class))).thenReturn(Page.empty());

        lessonService.getAllLessons(request, dto, Pageable.unpaged());

        assertNull(dto.getCategories());
        verify(lessonRepository).findAllLessons(eq(dto), any(Pageable.class));
    }

    // TC_LESSON_007
    @Test
    @DisplayName("TC_LESSON_007: createLesson throws when content is empty")
    void createLessonThrowsWhenContentIsEmpty() {
        LessonRequest req = LessonRequest.builder().courseId(10L).title("T").content("").videoUrl("url").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(WebToeicException.class, () -> lessonService.createLesson(request, req));
    }

    // TC_LESSON_008
    @Test
    @DisplayName("TC_LESSON_008: createLesson throws when title is empty")
    void createLessonThrowsWhenTitleIsEmpty() {
        LessonRequest req = LessonRequest.builder().courseId(10L).title("").content("Content").videoUrl("url").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(WebToeicException.class, () -> lessonService.createLesson(request, req));
    }

    // TC_LESSON_009
    @Test
    @DisplayName("TC_LESSON_009: createLesson throws when videoUrl is empty")
    void createLessonThrowsWhenVideoUrlIsEmpty() {
        LessonRequest req = LessonRequest.builder().courseId(10L).title("T").content("Content").videoUrl("").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(WebToeicException.class, () -> lessonService.createLesson(request, req));
    }

    // TC_LESSON_010
    @Test
    @DisplayName("TC_LESSON_010: createLesson throws when course not found")
    void createLessonThrowsWhenCourseNotFound() {
        LessonRequest req = LessonRequest.builder().courseId(99L).title("T").content("C").videoUrl("url").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> lessonService.createLesson(request, req));
    }

    // TC_LESSON_011
    @Test
    @DisplayName("TC_LESSON_011: createLesson saves lesson and returns response")
    void createLessonSavesLessonAndReturnsResponse() {
        LessonRequest req = LessonRequest.builder()
                .courseId(10L).title("Lesson 1").content("Content").videoUrl("http://video.url").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());
        when(convertUtil.convertLessonToDto(eq(request), eq(lesson), any())).thenReturn(lessonResponse);

        LessonResponse result = lessonService.createLesson(request, req);

        assertEquals(20L, result.getId());
        verify(lessonRepository).save(any(Lesson.class));
    }

    // TC_LESSON_012
    @Test
    @DisplayName("TC_LESSON_012: updateLesson throws when lesson not found")
    void updateLessonThrowsWhenLessonNotFound() {
        LessonRequest req = LessonRequest.builder().id(99L).build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> lessonService.updateLesson(request, req));
    }

    // TC_LESSON_013
    @Test
    @DisplayName("TC_LESSON_013: updateLesson throws when user has no permission")
    void updateLessonThrowsWhenNoPermission() {
        LessonRequest req = LessonRequest.builder().id(20L).build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));

        assertThrows(WebToeicException.class, () -> lessonService.updateLesson(request, req));
    }

    // TC_LESSON_014
    @Test
    @DisplayName("TC_LESSON_014: updateLesson succeeds for MANAGER")
    void updateLessonSucceedsForManager() {
        LessonRequest req = LessonRequest.builder().id(20L).title("Updated").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());
        when(convertUtil.convertLessonToDto(eq(request), eq(lesson), any())).thenReturn(lessonResponse);

        LessonResponse result = lessonService.updateLesson(request, req);

        assertEquals(20L, result.getId());
        verify(lessonRepository).save(lesson);
    }

    // TC_LESSON_015
    @Test
    @DisplayName("TC_LESSON_015: updateLesson succeeds for CONSULTANT who owns the lesson")
    void updateLessonSucceedsForConsultantOwner() {
        LessonRequest req = LessonRequest.builder().id(20L).title("Updated").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson)); // lesson.createdBy = consultantUser
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());
        when(convertUtil.convertLessonToDto(eq(request), eq(lesson), any())).thenReturn(lessonResponse);

        LessonResponse result = lessonService.updateLesson(request, req);

        assertEquals(20L, result.getId());
    }

    // TC_LESSON_016
    @Test
    @DisplayName("TC_LESSON_016: disableOrDelete throws when user has no permission")
    void disableOrDeleteThrowsWhenNoPermission() {
        LessonRequest req = LessonRequest.builder().id(20L).isActive(false).build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));

        assertThrows(WebToeicException.class, () -> lessonService.disableOrDelete(request, req));
    }

    // TC_LESSON_017
    @Test
    @DisplayName("TC_LESSON_017: disableOrDelete updates isActive and isDelete for MANAGER")
    void disableOrDeleteUpdatesFieldsForManager() {
        LessonRequest req = LessonRequest.builder().id(20L).isActive(false).isDelete(true).build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);
        when(attachDocumentLessonRepository.findAllByLessonId(20L)).thenReturn(List.of());
        when(convertUtil.convertLessonToDto(eq(request), eq(lesson), any())).thenReturn(lessonResponse);

        lessonService.disableOrDelete(request, req);

        assertEquals(false, lesson.getIsActive());
        assertEquals(true, lesson.getIsDelete());
        verify(lessonRepository).save(lesson);
    }

    // TC_LESSON_018
    @Test
    @DisplayName("TC_LESSON_018: updateLesson throws when CONSULTANT is not the owner")
    void updateLessonThrowsWhenConsultantIsNotOwner() {
        User otherConsultant = new User();
        otherConsultant.setId(5L);
        otherConsultant.setEmail("other@test.com");
        otherConsultant.setRole(ERole.CONSULTANT);

        LessonRequest req = LessonRequest.builder().id(20L).title("Updated").build();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherConsultant));
        when(lessonRepository.findById(20L)).thenReturn(Optional.of(lesson)); // lesson.createdBy = consultantUser (different)

        assertThrows(WebToeicException.class, () -> lessonService.updateLesson(request, req));
    }
}
