package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ECategoryCourse;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.CourseRequest;
import com.doan2025.webtoeic.dto.response.CourseResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.CourseRepository;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private ConvertUtil convertUtil;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private NotiUtils notiUtils;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private CourseServiceImpl courseService;

    private User managerUser;
    private User studentUser;
    private Course course;
    private CourseResponse courseResponse;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);

        studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);

        course = new Course();
        course.setId(10L);
        course.setTitle("TOEIC Listening");
        course.setPrice(100L);
        course.setCategoryCourse(ECategoryCourse.LISTENING);
        course.setIsActive(true);
        course.setIsDelete(false);

        courseResponse = new CourseResponse();
        courseResponse.setId(10L);
        courseResponse.setTitle("TOEIC Listening");
    }

    // TC_COURSE_001
    @Test
    @DisplayName("TC_COURSE_001: findByCourseBought returns page of bought courses for valid user")
    void findByCourseBoughtReturnsPageForValidUser() {
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(enrollmentRepository.findCourseByUser(eq(studentUser), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));
        when(convertUtil.convertCourseToDto(request, course)).thenReturn(courseResponse);

        Page<CourseResponse> result = courseService.findByCourseBought(request, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
    }

    // TC_COURSE_002
    @Test
    @DisplayName("TC_COURSE_002: findByCourseBought throws when user not found")
    void findByCourseBoughtThrowsWhenUserNotFound() {
        when(jwtUtil.getEmailFromToken(request)).thenReturn("unknown@test.com");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.findByCourseBought(request, Pageable.unpaged()));
    }

    // TC_COURSE_003
    @Test
    @DisplayName("TC_COURSE_003: getCourseDetail returns course response for valid id")
    void getCourseDetailReturnsResponseForValidId() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(convertUtil.convertCourseToDto(request, course)).thenReturn(courseResponse);

        CourseResponse result = courseService.getCourseDetail(request, 10L);

        assertEquals(10L, result.getId());
    }

    // TC_COURSE_004
    @Test
    @DisplayName("TC_COURSE_004: getCourseDetail throws when course not found")
    void getCourseDetailThrowsWhenCourseNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.getCourseDetail(request, 99L));
    }

    // TC_COURSE_005
    @Test
    @DisplayName("TC_COURSE_005: getCourses sets categories null when empty list provided")
    void getCoursesSetsCategoriesNullWhenEmptyList() {
        SearchBaseDto dto = new SearchBaseDto();
        dto.setCategories(List.of());
        when(request.getHeader("Authorization")).thenReturn(null);
        when(courseRepository.findCourses(eq(dto), eq(""), any(Pageable.class))).thenReturn(Page.empty());

        courseService.getCourses(request, dto, Pageable.unpaged());

        assertNull(dto.getCategories());
        verify(courseRepository).findCourses(eq(dto), eq(""), any(Pageable.class));
    }

    // TC_COURSE_006
    @Test
    @DisplayName("TC_COURSE_006: getCourses extracts email from Bearer token")
    void getCoursesExtractsEmailFromBearerToken() {
        SearchBaseDto dto = new SearchBaseDto();
        when(request.getHeader("Authorization")).thenReturn("Bearer some.jwt.token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(courseRepository.findCourses(eq(dto), eq("student@test.com"), any(Pageable.class))).thenReturn(Page.empty());

        courseService.getCourses(request, dto, Pageable.unpaged());

        verify(courseRepository).findCourses(eq(dto), eq("student@test.com"), any(Pageable.class));
    }

    // TC_COURSE_007
    @Test
    @DisplayName("TC_COURSE_007: getAllCourses sets categories null when empty list provided")
    void getAllCoursesSetsCategoriesNullWhenEmptyList() {
        SearchBaseDto dto = new SearchBaseDto();
        dto.setCategories(List.of());
        when(courseRepository.findAllCourses(eq(dto), any(Pageable.class))).thenReturn(Page.empty());

        courseService.getAllCourses(request, dto, Pageable.unpaged());

        assertNull(dto.getCategories());
        verify(courseRepository).findAllCourses(eq(dto), any(Pageable.class));
    }

    // TC_COURSE_008
    @Test
    @DisplayName("TC_COURSE_008: getOwnCourses sets email from token and queries own courses")
    void getOwnCoursesSetsEmailAndQueriesOwnCourses() {
        SearchBaseDto dto = new SearchBaseDto();
        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(courseRepository.findOwnCourses(eq(dto), eq("student@test.com"), any(Pageable.class))).thenReturn(Page.empty());

        courseService.getOwnCourses(request, dto, Pageable.unpaged());

        assertEquals("student@test.com", dto.getEmail());
        verify(courseRepository).findOwnCourses(eq(dto), eq("student@test.com"), any(Pageable.class));
    }

    // TC_COURSE_009
    @Test
    @DisplayName("TC_COURSE_009: createCourse throws when categoryId is null")
    void createCourseThrowsWhenCategoryIdIsNull() {
        CourseRequest req = new CourseRequest();
        req.setAuthorId(1L);
        req.setTitle("Test");
        req.setPrice(100L);

        assertThrows(WebToeicException.class, () -> courseService.createCourse(request, req));
    }

    // TC_COURSE_010
    @Test
    @DisplayName("TC_COURSE_010: createCourse throws when authorId is null")
    void createCourseThrowsWhenAuthorIdIsNull() {
        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setTitle("Test");
        req.setPrice(100L);

        assertThrows(WebToeicException.class, () -> courseService.createCourse(request, req));
    }

    // TC_COURSE_011
    @Test
    @DisplayName("TC_COURSE_011: createCourse throws when title is empty")
    void createCourseThrowsWhenTitleIsEmpty() {
        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setAuthorId(1L);
        req.setTitle("");
        req.setPrice(100L);

        assertThrows(WebToeicException.class, () -> courseService.createCourse(request, req));
    }

    // TC_COURSE_012
    @Test
    @DisplayName("TC_COURSE_012: createCourse throws when price is zero or negative")
    void createCourseThrowsWhenPriceIsInvalid() {
        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setAuthorId(1L);
        req.setTitle("Test");
        req.setPrice(0L);

        assertThrows(WebToeicException.class, () -> courseService.createCourse(request, req));
    }

    // TC_COURSE_013
    @Test
    @DisplayName("TC_COURSE_013: createCourse saves course and sends notification")
    void createCourseSavesCourseAndSendsNotification() {
        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setAuthorId(1L);
        req.setTitle("TOEIC Listening");
        req.setPrice(200L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(managerUser));
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(userRepository.findUserOnlyStudent()).thenReturn(List.of(studentUser));
        when(convertUtil.convertCourseToDto(eq(request), any(Course.class))).thenReturn(courseResponse);

        CourseResponse result = courseService.createCourse(request, req);

        assertEquals(10L, result.getId());
        verify(courseRepository).save(any(Course.class));
        verify(notiUtils).sendNoti(any(), any(), any(), any(), any());
    }

    // TC_COURSE_014
    @Test
    @DisplayName("TC_COURSE_014: updateCourse throws when user not found")
    void updateCourseThrowsWhenUserNotFound() {
        CourseRequest req = new CourseRequest();
        req.setId(10L);
        req.setCategoryId(1);
        req.setAuthorId(1L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("unknown@test.com");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.updateCourse(request, req));
    }

    // TC_COURSE_015
    @Test
    @DisplayName("TC_COURSE_015: updateCourse throws when course not found")
    void updateCourseThrowsWhenCourseNotFound() {
        CourseRequest req = new CourseRequest();
        req.setId(99L);
        req.setCategoryId(1);
        req.setAuthorId(1L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.updateCourse(request, req));
    }

    // TC_COURSE_016
    @Test
    @DisplayName("TC_COURSE_016: updateCourse saves and returns updated course")
    void updateCourseSavesAndReturnsUpdatedCourse() {
        CourseRequest req = new CourseRequest();
        req.setId(10L);
        req.setTitle("Updated Title");
        req.setPrice(300L);
        req.setCategoryId(1);
        req.setAuthorId(1L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(managerUser));
        when(courseRepository.save(course)).thenReturn(course);
        when(convertUtil.convertCourseToDto(request, course)).thenReturn(courseResponse);

        CourseResponse result = courseService.updateCourse(request, req);

        assertEquals(10L, result.getId());
        verify(courseRepository).save(course);
    }

    // TC_COURSE_017
    @Test
    @DisplayName("TC_COURSE_017: disableOrDeleteCourse throws when user not found")
    void disableOrDeleteCourseThrowsWhenUserNotFound() {
        CourseRequest req = new CourseRequest();
        req.setId(10L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("unknown@test.com");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.disableOrDeleteCourse(request, req));
    }

    // TC_COURSE_018
    @Test
    @DisplayName("TC_COURSE_018: disableOrDeleteCourse throws when course not found")
    void disableOrDeleteCourseThrowsWhenCourseNotFound() {
        CourseRequest req = new CourseRequest();
        req.setId(99L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(WebToeicException.class, () -> courseService.disableOrDeleteCourse(request, req));
    }

    // TC_COURSE_019
    @Test
    @DisplayName("TC_COURSE_019: disableOrDeleteCourse throws when user is not MANAGER")
    void disableOrDeleteCourseThrowsWhenNotManager() {
        CourseRequest req = new CourseRequest();
        req.setId(10L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(WebToeicException.class, () -> courseService.disableOrDeleteCourse(request, req));
    }

    // TC_COURSE_019b
    @Test
    @DisplayName("TC_COURSE_019b: createCourse succeeds for CONSULTANT role")
    void createCourseSucceedsForConsultant() {
        User consultantUser = new User();
        consultantUser.setId(4L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);

        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setAuthorId(4L);
        req.setTitle("TOEIC Reading");
        req.setPrice(150L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findById(4L)).thenReturn(Optional.of(consultantUser));
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(userRepository.findUserOnlyStudent()).thenReturn(List.of(studentUser));
        when(convertUtil.convertCourseToDto(eq(request), any(Course.class))).thenReturn(courseResponse);

        CourseResponse result = courseService.createCourse(request, req);

        assertEquals(10L, result.getId());
        verify(courseRepository).save(any(Course.class));
    }

    // TC_COURSE_020
    @Test
    @DisplayName("TC_COURSE_020: disableOrDeleteCourse updates isActive and isDelete when MANAGER")
    void disableOrDeleteCourseUpdatesFieldsWhenManager() {
        CourseRequest req = new CourseRequest();
        req.setId(10L);
        req.setIsActive(false);
        req.setIsDelete(true);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(convertUtil.convertCourseToDto(request, course)).thenReturn(courseResponse);

        courseService.disableOrDeleteCourse(request, req);

        assertEquals(false, course.getIsActive());
        assertEquals(true, course.getIsDelete());
        verify(courseRepository).save(course);
    }

    // TC_COURSE_021 - FAIL: price=null causes NullPointerException instead of WebToeicException
    @Test
    @DisplayName("TC_COURSE_021: createCourse with null price should throw WebToeicException NOT_AVAILABLE")
    void createCourseThrowsWhenPriceIsNull() {
        CourseRequest req = new CourseRequest();
        req.setCategoryId(1);
        req.setAuthorId(1L);
        req.setTitle("Test");
        req.setPrice(null);

        // Expected: WebToeicException NOT_AVAILABLE
        // Actual: NullPointerException - code does (request.getPrice() <= 0) unboxing null Long → BUG
        assertThrows(WebToeicException.class, () -> courseService.createCourse(request, req));
    }

    // TC_COURSE_023 - FAIL: controller allows CONSULTANT to call update-status but service throws NOT_PERMISSION
    @Test
    @DisplayName("TC_COURSE_023: disableOrDeleteCourse by CONSULTANT should throw NOT_PERMISSION (controller/service inconsistency)")
    void disableOrDeleteCourseThrowsForConsultant() {
        User consultantUser = new User();
        consultantUser.setId(5L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);

        CourseRequest req = new CourseRequest();
        req.setId(10L);
        req.setIsActive(false);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        // This PASSES at service level (throws NOT_PERMISSION as expected)
        // But controller @PreAuthorize("CONSULTANT OR MANAGER") allows CONSULTANT in → inconsistency
        assertThrows(WebToeicException.class, () -> courseService.disableOrDeleteCourse(request, req));
    }
}
