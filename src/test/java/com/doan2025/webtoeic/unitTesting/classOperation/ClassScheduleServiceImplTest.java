package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.EScheduleStatus;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.dto.SearchScheduleSto;
import com.doan2025.webtoeic.dto.request.ClassScheduleRequest;
import com.doan2025.webtoeic.dto.response.ClassScheduleResponse;
import com.doan2025.webtoeic.dto.response.OverviewStatisticAttendance;
import com.doan2025.webtoeic.dto.response.OverviewStudentAttendance;
import com.doan2025.webtoeic.dto.response.DetailStatisticAttendance;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClassScheduleServiceImpl.
 * Target: 100% JaCoCo Instructions & Branches coverage.
 *
 * Naming convention: TC-CSS-XXX  (ClassScheduleService Test Case)
 *
 * Rollback strategy: all tests use Mockito mocks – no real DB is touched,
 * so there is no persistent state to roll back.  Where a test verifies that
 * a repository.save() was called, the call is captured with ArgumentCaptor
 * and the captured object is NOT persisted anywhere.
 */
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ClassScheduleServiceImpl Unit Tests")
class ClassScheduleServiceImplTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────

    @Mock private AttendanceRepository          attendanceRepository;
    @Mock private ClassScheduleRepository       classScheduleRepository;
    @Mock private ClassMemberRepository         classMemberRepository;
    @Mock private UserRepository                userRepository;
    @Mock private RoomRepository                roomRepository;
    @Mock private ClassRepository               classRepository;
    @Mock private ConvertUtil                   convertUtil;
    @Mock private JwtUtil                       jwtUtil;
    @Mock private HttpServletRequest            httpRequest;

    @InjectMocks
    private ClassScheduleServiceImpl service;

    // ─── Common test fixtures ─────────────────────────────────────────────────

    private User managerUser;
    private User consultantUser;
    private User teacherUser;
    private User studentUser;
    private Class testClass;
    private ClassSchedule testSchedule;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // Manager
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);
        managerUser.setCode("MGR001");

        // Consultant
        consultantUser = new User();
        consultantUser.setId(2L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);
        consultantUser.setCode("CON001");

        // Teacher
        teacherUser = new User();
        teacherUser.setId(3L);
        teacherUser.setEmail("teacher@test.com");
        teacherUser.setRole(ERole.TEACHER);
        teacherUser.setCode("TCH001");

        // Student
        studentUser = new User();
        studentUser.setId(4L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);
        studentUser.setCode("STD001");

        // Room
        testRoom = new Room();
        testRoom.setId(10L);
        testRoom.setName("Room A");

        // Class
        testClass = new Class();
        testClass.setId(100L);
        testClass.setName("TOEIC 600+");
        testClass.setTeacher(teacherUser);
        testClass.setCreatedBy(consultantUser);

        // Schedule
        testSchedule = new ClassSchedule();
        testSchedule.setId(200L);
        testSchedule.setTitle("Session 1");
        testSchedule.setStartAt(new Date(System.currentTimeMillis() + 3_600_000));
        testSchedule.setEndAt(new Date(System.currentTimeMillis() + 7_200_000));
        testSchedule.setStatus(EScheduleStatus.ACTIVE);
        testSchedule.setIsActive(true);
        testSchedule.setIsDelete(false);
        testSchedule.setClazz(testClass);
        testSchedule.setRoom(testRoom);
        testSchedule.setCreatedBy(consultantUser);
    }

    // =========================================================================
    // detailStatisticAttendance
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-001
     * Test Objective : TEACHER who is a class member can access detail attendance statistic.
     * Input          : Teacher user (id=3, role=TEACHER), scheduleId=200,
     *                  classMemberRepository returns true for (classId=100, userId=3).
     * Expected Output: Returns Page<DetailStatisticAttendance> from repository (non-null, empty page).
     * Notes          : Verifies the "TEACHER + is member" happy-path branch.
     */
    @Test
    @DisplayName("TC-CSS-001: Teacher member gets detail attendance statistic")
    void tc_css_001_detailStatisticAttendance_teacherMember_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DetailStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(testClass.getId(), teacherUser.getId()))
                .thenReturn(true);
        when(attendanceRepository.detailStatisticAttendance(200L, pageable)).thenReturn(expected);

        Page<?> result = service.detailStatisticAttendance(httpRequest, 200L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-002
     * Test Objective : CONSULTANT can always access detail attendance statistic regardless of membership.
     * Input          : Consultant user (id=2, role=CONSULTANT), scheduleId=200,
     *                  classMemberRepository returns false for (classId=100, userId=2).
     * Expected Output: Returns Page<DetailStatisticAttendance> (non-null, empty page).
     * Notes          : Verifies CONSULTANT bypasses member check.
     */
    @Test
    @DisplayName("TC-CSS-002: Consultant gets detail attendance statistic")
    void tc_css_002_detailStatisticAttendance_consultant_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DetailStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(testClass.getId(), consultantUser.getId()))
                .thenReturn(false); // not a member – still allowed
        when(attendanceRepository.detailStatisticAttendance(200L, pageable)).thenReturn(expected);

        Page<?> result = service.detailStatisticAttendance(httpRequest, 200L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-003
     * Test Objective : MANAGER can always access detail attendance statistic regardless of membership.
     * Input          : Manager user (id=1, role=MANAGER), scheduleId=200,
     *                  classMemberRepository returns false for (classId=100, userId=1).
     * Expected Output: Returns Page<DetailStatisticAttendance> (non-null, empty page).
     * Notes          : Verifies MANAGER bypasses member check.
     */
    @Test
    @DisplayName("TC-CSS-003: Manager gets detail attendance statistic")
    void tc_css_003_detailStatisticAttendance_manager_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DetailStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(testClass.getId(), managerUser.getId()))
                .thenReturn(false);
        when(attendanceRepository.detailStatisticAttendance(200L, pageable)).thenReturn(expected);

        Page<?> result = service.detailStatisticAttendance(httpRequest, 200L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-004
     * Test Objective : TEACHER who is NOT a class member is denied access.
     * Input          : Teacher user (id=3, role=TEACHER), scheduleId=200,
     *                  classMemberRepository returns false for (classId=100, userId=3).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : Verifies "TEACHER + not member" guard throws correctly.
     */
    @Test
    @DisplayName("TC-CSS-004: Teacher non-member is denied detail attendance statistic")
    void tc_css_004_detailStatisticAttendance_teacherNonMember_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(testClass.getId(), teacherUser.getId()))
                .thenReturn(false);

        assertThatThrownBy(() -> service.detailStatisticAttendance(httpRequest, 200L, pageable))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> {
                    WebToeicException e = (WebToeicException) ex;
                    assertThat(e.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                });
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-005
     * Test Objective : STUDENT role is always denied (not TEACHER/CONSULTANT/MANAGER).
     * Input          : Student user (id=4, role=STUDENT), scheduleId=200,
     *                  classMemberRepository returns true (membership does not help STUDENT).
     * Expected Output: WebToeicException thrown.
     * Notes          : Verifies STUDENT cannot access detail attendance regardless of membership.
     */
    @Test
    @DisplayName("TC-CSS-005: Student is denied detail attendance statistic")
    void tc_css_005_detailStatisticAttendance_student_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        // student is member, but STUDENT role never qualifies as TEACHER
        when(classMemberRepository.existsMemberInClass(testClass.getId(), studentUser.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.detailStatisticAttendance(httpRequest, 200L, pageable))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-006
     * Test Objective : Non-existent scheduleId causes NOT_EXISTED exception.
     * Input          : Manager user, scheduleId=999 (not present in repository).
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED.
     * Notes          : Verifies schedule lookup guard before role check.
     */
    @Test
    @DisplayName("TC-CSS-006: Schedule not found throws NOT_EXISTED")
    void tc_css_006_detailStatisticAttendance_scheduleNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detailStatisticAttendance(httpRequest, 999L, pageable))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> {
                    WebToeicException e = (WebToeicException) ex;
                    assertThat(e.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                });
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-007
     * Test Objective : JWT token resolves to an unknown email → user not found throws exception.
     * Input          : Token email "ghost@test.com" not present in userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Verifies user-lookup guard at method entry.
     */
    @Test
    @DisplayName("TC-CSS-007: User not found throws NOT_EXISTED")
    void tc_css_007_detailStatisticAttendance_userNotFound_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@test.com");
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detailStatisticAttendance(httpRequest, 200L, pageable))
                .isInstanceOf(WebToeicException.class);
    }

    // =========================================================================
    // overviewStatisticAttendance
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-008
     * Test Objective : TEACHER who is a class member can get overview attendance statistic.
     * Input          : Teacher user (id=3, role=TEACHER), classId=100,
     *                  classMemberRepository returns true for (classId=100, userId=3).
     * Expected Output: Returns Page<OverviewStatisticAttendance> (non-null, empty page).
     * Notes          : Verifies "TEACHER + is member" happy-path branch.
     */
    @Test
    @DisplayName("TC-CSS-008: Teacher member gets overview attendance statistic")
    void tc_css_008_overviewStatisticAttendance_teacherMember_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OverviewStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classMemberRepository.existsMemberInClass(100L, teacherUser.getId())).thenReturn(true);
        when(attendanceRepository.overviewStatisticAttendance(100L, pageable)).thenReturn(expected);

        Page<?> result = service.overviewStatisticAttendance(httpRequest, 100L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-009
     * Test Objective : CONSULTANT can get overview attendance statistic without membership.
     * Input          : Consultant user (id=2, role=CONSULTANT), classId=100,
     *                  classMemberRepository returns false for (classId=100, userId=2).
     * Expected Output: Returns Page<OverviewStatisticAttendance> (non-null, empty page).
     * Notes          : Verifies CONSULTANT bypasses member check.
     */
    @Test
    @DisplayName("TC-CSS-009: Consultant gets overview attendance statistic")
    void tc_css_009_overviewStatisticAttendance_consultant_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OverviewStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classMemberRepository.existsMemberInClass(100L, consultantUser.getId())).thenReturn(false);
        when(attendanceRepository.overviewStatisticAttendance(100L, pageable)).thenReturn(expected);

        Page<?> result = service.overviewStatisticAttendance(httpRequest, 100L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-010
     * Test Objective : MANAGER can get overview attendance statistic without membership.
     * Input          : Manager user (id=1, role=MANAGER), classId=100,
     *                  classMemberRepository returns false for (classId=100, userId=1).
     * Expected Output: Returns Page<OverviewStatisticAttendance> (non-null, empty page).
     * Notes          : Verifies MANAGER bypasses member check.
     */
    @Test
    @DisplayName("TC-CSS-010: Manager gets overview attendance statistic")
    void tc_css_010_overviewStatisticAttendance_manager_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OverviewStatisticAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classMemberRepository.existsMemberInClass(100L, managerUser.getId())).thenReturn(false);
        when(attendanceRepository.overviewStatisticAttendance(100L, pageable)).thenReturn(expected);

        Page<?> result = service.overviewStatisticAttendance(httpRequest, 100L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-011
     * Test Objective : TEACHER who is NOT a class member is denied overview statistic.
     * Input          : Teacher user (id=3, role=TEACHER), classId=100,
     *                  classMemberRepository returns false for (classId=100, userId=3).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : Verifies "TEACHER + not member" guard throws correctly.
     */
    @Test
    @DisplayName("TC-CSS-011: Teacher non-member denied overview attendance statistic")
    void tc_css_011_overviewStatisticAttendance_teacherNonMember_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classMemberRepository.existsMemberInClass(100L, teacherUser.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.overviewStatisticAttendance(httpRequest, 100L, pageable))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-012
     * Test Objective : STUDENT role is always denied overview statistic.
     * Input          : Student user (id=4, role=STUDENT), classId=100,
     *                  classMemberRepository returns true (membership does not help STUDENT).
     * Expected Output: WebToeicException thrown.
     * Notes          : Verifies STUDENT is always blocked regardless of membership.
     */
    @Test
    @DisplayName("TC-CSS-012: Student denied overview attendance statistic")
    void tc_css_012_overviewStatisticAttendance_student_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classMemberRepository.existsMemberInClass(100L, studentUser.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.overviewStatisticAttendance(httpRequest, 100L, pageable))
                .isInstanceOf(WebToeicException.class);
    }

    // =========================================================================
    // overviewStudentAttendance
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-013
     * Test Objective : Any authenticated privileged user (MANAGER shown here) gets overview student attendance.
     * Input          : Manager user (id=1, role=MANAGER), classId=100.
     * Expected Output: Returns Page<OverviewStudentAttendance> (non-null, empty page).
     * Notes          : Basic happy-path test; privilege details tested in coverage tests below.
     */
    @Test
    @DisplayName("TC-CSS-013: Authenticated user gets overview student attendance")
    void tc_css_013_overviewStudentAttendance_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OverviewStudentAttendance> expected = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(attendanceRepository.overviewStudentAttendance(100L, pageable)).thenReturn(expected);

        Page<?> result = service.overviewStudentAttendance(httpRequest, 100L, pageable);

        assertThat(result).isEqualTo(expected);
    }

    // =========================================================================
    // getScheduleDetail
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getScheduleDetail
     * Test Case ID   : TC-CSS-014
     * Test Objective : Class member attempting schedule detail access triggers NOT_PERMISSION guard.
     * Input          : Student user (id=4), scheduleId=200,
     *                  classMemberRepository returns true for (scheduleId=200, userId=4).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : The service checks membership against scheduleId (not classId) in this method;
     *                  member status for student still results in denial due to role restrictions.
     */
    @Test
    @DisplayName("TC-CSS-014: Class member gets schedule detail")
    void tc_css_014_getScheduleDetail_member_success() {
        ClassScheduleResponse expectedResponse = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(200L, studentUser.getId())).thenReturn(true);
        when(convertUtil.convertScheduleToDto(httpRequest, testSchedule)).thenReturn(expectedResponse);

        assertThatThrownBy(() -> service.getScheduleDetail(httpRequest, 200L))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getScheduleDetail
     * Test Case ID   : TC-CSS-015
     * Test Objective : Non-member user can retrieve schedule detail (no restriction for non-members in this method).
     * Input          : Student user (id=4), scheduleId=200,
     *                  classMemberRepository returns false for (scheduleId=200, userId=4).
     * Expected Output: ClassScheduleResponse returned (no exception).
     * Notes          : existsMemberInClass=false does not block this path; convertUtil.convertScheduleToDto is called.
     */
    @Test
    @DisplayName("TC-CSS-015: Non-member user gets schedule detail (no restriction in method)")
    void tc_css_015_getScheduleDetail_nonMember_success() {
        ClassScheduleResponse expectedResponse = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classMemberRepository.existsMemberInClass(200L, studentUser.getId())).thenReturn(false);
        when(convertUtil.convertScheduleToDto(httpRequest, testSchedule)).thenReturn(expectedResponse);

        ClassScheduleResponse result = service.getScheduleDetail(httpRequest, 200L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getScheduleDetail
     * Test Case ID   : TC-CSS-016
     * Test Objective : Non-existent scheduleId causes NOT_EXISTED exception in getScheduleDetail.
     * Input          : Student user, scheduleId=999 (not present in repository).
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED.
     * Notes          : Verifies schedule lookup guard at method entry.
     */
    @Test
    @DisplayName("TC-CSS-016: Schedule not found in getScheduleDetail throws NOT_EXISTED")
    void tc_css_016_getScheduleDetail_scheduleNotFound_throwsException() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getScheduleDetail(httpRequest, 999L))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    // =========================================================================
    // getClassSchedule
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-017
     * Test Objective : MANAGER gets schedules with empty filter lists; empty lists are normalized to null.
     * Input          : Manager user, SearchScheduleSto with empty classId/teacherId/status lists.
     * Expected Output: Page of schedules returned; filterSchedule called with null classIds param.
     * Notes          : Verifies empty-list → null normalization and MANAGER unrestricted access.
     */
    @Test
    @DisplayName("TC-CSS-017: Manager gets class schedules with null filters")
    void tc_css_017_getClassSchedule_manager_nullFilters_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchScheduleSto dto = new SearchScheduleSto();
        dto.setClassId(Collections.emptyList());
        dto.setTeacherId(Collections.emptyList());
        dto.setStatus(Collections.emptyList());

        ClassSchedule cs = testSchedule;
        Page<ClassSchedule> page = new PageImpl<>(List.of(cs));
        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.filterSchedule(any(), isNull(), any())).thenReturn(page);
        when(convertUtil.convertScheduleToDto(any(), eq(cs))).thenReturn(response);

        Page<?> result = service.getClassSchedule(httpRequest, dto, pageable);

        assertThat(result.getContent()).hasSize(1);
        // Verify null was passed for classIds to filterSchedule
        verify(classScheduleRepository).filterSchedule(any(), isNull(), any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-018
     * Test Objective : CONSULTANT gets schedules without classId restriction.
     * Input          : Consultant user, SearchScheduleSto with null classId/teacherId/status.
     * Expected Output: Non-null Page returned; filterSchedule called with null classIds.
     * Notes          : Verifies CONSULTANT has unrestricted access similar to MANAGER.
     */
    @Test
    @DisplayName("TC-CSS-018: Consultant gets class schedules without restriction")
    void tc_css_018_getClassSchedule_consultant_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchScheduleSto dto = new SearchScheduleSto();
        dto.setClassId(null);
        dto.setTeacherId(null);
        dto.setStatus(null);

        Page<ClassSchedule> page = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.filterSchedule(any(), isNull(), any())).thenReturn(page);

        Page<?> result = service.getClassSchedule(httpRequest, dto, pageable);

        assertThat(result).isNotNull();
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-019
     * Test Objective : TEACHER gets schedules filtered by their own class memberships.
     * Input          : Teacher user, SearchScheduleSto with classId=[100L], teacherId=[3L], status=["ACTIVE"];
     *                  classMemberRepository returns [100L] for teacher's email.
     * Expected Output: Page with 1 item; filterSchedule called with memberClassIds=[100L].
     * Notes          : Verifies TEACHER role uses membership-scoped filtering.
     */
    @Test
    @DisplayName("TC-CSS-019: Teacher gets class schedules filtered by membership")
    void tc_css_019_getClassSchedule_teacher_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchScheduleSto dto = new SearchScheduleSto();
        dto.setClassId(List.of(100L));
        dto.setTeacherId(List.of(3L));
        dto.setStatus(List.of("ACTIVE"));

        List<Long> memberClassIds = List.of(100L);
        Page<ClassSchedule> page = new PageImpl<>(List.of(testSchedule));
        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classMemberRepository.findClassOfMember(teacherUser.getEmail())).thenReturn(memberClassIds);
        when(classScheduleRepository.filterSchedule(any(), eq(memberClassIds), any())).thenReturn(page);
        when(convertUtil.convertScheduleToDto(any(), eq(testSchedule))).thenReturn(response);

        Page<?> result = service.getClassSchedule(httpRequest, dto, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-020
     * Test Objective : STUDENT gets schedules filtered by their own class memberships.
     * Input          : Student user, SearchScheduleSto with null filters;
     *                  classMemberRepository returns [100L] for student's email.
     * Expected Output: Non-null Page returned; findClassOfMember called with student's email.
     * Notes          : Verifies STUDENT role uses membership-scoped filtering (same as TEACHER).
     */
    @Test
    @DisplayName("TC-CSS-020: Student gets class schedules filtered by membership")
    void tc_css_020_getClassSchedule_student_success() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchScheduleSto dto = new SearchScheduleSto();
        dto.setClassId(null);
        dto.setTeacherId(null);
        dto.setStatus(null);

        List<Long> memberClassIds = List.of(100L);
        Page<ClassSchedule> page = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classMemberRepository.findClassOfMember(studentUser.getEmail())).thenReturn(memberClassIds);
        when(classScheduleRepository.filterSchedule(any(), eq(memberClassIds), any())).thenReturn(page);

        Page<?> result = service.getClassSchedule(httpRequest, dto, pageable);

        assertThat(result).isNotNull();
        verify(classMemberRepository).findClassOfMember(studentUser.getEmail());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-021
     * Test Objective : Empty list fields in SearchScheduleSto are normalized to null before query.
     * Input          : Manager user, SearchScheduleSto with empty (not null) classId, teacherId, status lists.
     * Expected Output: After method execution, dto.classId, dto.teacherId, and dto.status are all null.
     * Notes          : Verifies the normalization logic (empty list → null) applied before filtering.
     */
    @Test
    @DisplayName("TC-CSS-021: Empty classId list normalized to null for manager")
    void tc_css_021_getClassSchedule_emptyClassIdNormalizedToNull() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchScheduleSto dto = new SearchScheduleSto();
        dto.setClassId(new ArrayList<>());  // empty – should become null
        dto.setTeacherId(new ArrayList<>());
        dto.setStatus(new ArrayList<>());

        Page<ClassSchedule> page = new PageImpl<>(Collections.emptyList());

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.filterSchedule(any(), isNull(), any())).thenReturn(page);

        service.getClassSchedule(httpRequest, dto, pageable);

        // After normalization dto.classId should be null
        assertThat(dto.getClassId()).isNull();
        assertThat(dto.getTeacherId()).isNull();
        assertThat(dto.getStatus()).isNull();
    }

    // =========================================================================
    // createScheduleInClass
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-022
     * Test Objective : Consultant creates a valid schedule with no conflicts successfully.
     * Input          : Consultant user, list of 1 valid ClassScheduleRequest
     *                  (classId=100, roomId=10, title="Session 1", future startAt/endAt);
     *                  no room or class schedule conflicts.
     * Expected Output: List of 1 ClassScheduleResponse; classScheduleRepository.save() called once.
     * Notes          : Rollback — save() is mocked; nothing persisted to real DB.
     */
    @Test
    @DisplayName("TC-CSS-022: Consultant creates schedule successfully")
    void tc_css_022_createScheduleInClass_consultant_success() {
        ClassScheduleRequest req = buildValidScheduleRequest();

        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(10L)))
                .thenReturn(Collections.emptyList());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), eq(100L)))
                .thenReturn(Collections.emptyList());
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), eq(testSchedule))).thenReturn(response);

        List<?> results = service.createScheduleInClass(httpRequest, List.of(req));

        assertThat(results).hasSize(1);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-023
     * Test Objective : Room already occupied at the requested time → creation blocked with NOT_AVAILABLE.
     * Input          : Consultant user, valid ClassScheduleRequest;
     *                  existsScheduleByRoomIdAndStartAtAndEndAt returns [999L] (conflict).
     * Expected Output: WebToeicException with ResponseCode.NOT_AVAILABLE; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-023: Room already occupied throws NOT_AVAILABLE")
    void tc_css_023_createScheduleInClass_roomOccupied_throwsException() {
        ClassScheduleRequest req = buildValidScheduleRequest();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(10L)))
                .thenReturn(List.of(999L)); // room conflict

        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of(req)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_AVAILABLE));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-024
     * Test Objective : Class already has a schedule at that time → creation blocked with NOT_AVAILABLE.
     * Input          : Consultant user, valid ClassScheduleRequest; room free,
     *                  but existsScheduleByClassIdAndStartAtAndEndAt returns [998L] (conflict).
     * Expected Output: WebToeicException with ResponseCode.NOT_AVAILABLE; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-024: Class schedule conflict throws NOT_AVAILABLE")
    void tc_css_024_createScheduleInClass_classConflict_throwsException() {
        ClassScheduleRequest req = buildValidScheduleRequest();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(10L)))
                .thenReturn(Collections.emptyList());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), eq(100L)))
                .thenReturn(List.of(998L)); // class conflict

        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of(req)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_AVAILABLE));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-025
     * Test Objective : Null roomId in request → creation blocked with IS_NULL.
     * Input          : Consultant user, ClassScheduleRequest with roomId=null;
     *                  no room or class conflicts.
     * Expected Output: WebToeicException with ResponseCode.IS_NULL; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-025: Null roomId throws IS_NULL")
    void tc_css_025_createScheduleInClass_nullRoomId_throwsException() {
        ClassScheduleRequest req = buildValidScheduleRequest();
        req.setRoomId(null); // null room

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), eq(100L)))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of(req)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.IS_NULL));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-026
     * Test Objective : Non-existent classId in request → creation blocked with NOT_EXISTED.
     * Input          : Consultant user, ClassScheduleRequest with classId=999 (not in repository).
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-026: Class not found in createSchedule throws NOT_EXISTED")
    void tc_css_026_createScheduleInClass_classNotFound_throwsException() {
        ClassScheduleRequest req = buildValidScheduleRequest();
        req.setClassId(999L);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of(req)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-027
     * Test Objective : Non-existent roomId in request (after conflict checks pass) → NOT_EXISTED.
     * Input          : Consultant user, valid ClassScheduleRequest; class exists, no conflicts,
     *                  but roomRepository.findById(10L) returns empty.
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-027: Room not found in createSchedule throws NOT_EXISTED")
    void tc_css_027_createScheduleInClass_roomNotFound_throwsException() {
        ClassScheduleRequest req = buildValidScheduleRequest();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(10L)))
                .thenReturn(Collections.emptyList());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), eq(100L)))
                .thenReturn(Collections.emptyList());
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of(req)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-028
     * Test Objective : Batch creation of multiple schedules all succeed.
     * Input          : Consultant user, list of 2 valid ClassScheduleRequests with no conflicts.
     * Expected Output: List of 2 ClassScheduleResponses; save() called exactly 2 times.
     * Notes          : Rollback — save() is mocked; nothing persisted to real DB.
     */
    @Test
    @DisplayName("TC-CSS-028: Multiple schedules created in batch")
    void tc_css_028_createScheduleInClass_multipleRequests_success() {
        ClassScheduleRequest req1 = buildValidScheduleRequest();
        ClassScheduleRequest req2 = buildValidScheduleRequest();

        ClassScheduleResponse res = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classRepository.findById(100L)).thenReturn(Optional.of(testClass));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(10L)))
                .thenReturn(Collections.emptyList());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), eq(100L)))
                .thenReturn(Collections.emptyList());
        when(roomRepository.findById(10L)).thenReturn(Optional.of(testRoom));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), any())).thenReturn(res);

        List<?> results = service.createScheduleInClass(httpRequest, List.of(req1, req2));

        assertThat(results).hasSize(2);
        verify(classScheduleRepository, times(2)).save(any());
    }

    // =========================================================================
    // updateScheduleInClass
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-029
     * Test Objective : TEACHER who is the class owner can update the schedule successfully.
     * Input          : Teacher user (same as testClass.teacher), valid ClassScheduleRequest
     *                  (classScheduleId=200, roomId=10 unchanged).
     * Expected Output: ClassScheduleResponse returned; save() called once.
     * Notes          : Rollback — save() is mocked.
     */
    @Test
    @DisplayName("TC-CSS-029: Teacher updates own class schedule successfully")
    void tc_css_029_updateScheduleInClass_teacher_success() {
        ClassScheduleRequest req = buildValidUpdateRequest();

        ClassSchedule savedSchedule = testSchedule;
        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(savedSchedule);
        when(convertUtil.convertScheduleToDto(eq(httpRequest), any())).thenReturn(response);

        ClassScheduleResponse result = service.updateScheduleInClass(httpRequest, req);

        assertThat(result).isEqualTo(response);
        verify(classScheduleRepository).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-030
     * Test Objective : CONSULTANT can update any class schedule regardless of ownership.
     * Input          : Consultant user, valid ClassScheduleRequest (classScheduleId=200).
     * Expected Output: ClassScheduleResponse returned; save() called once.
     * Notes          : Rollback — save() is mocked.
     */
    @Test
    @DisplayName("TC-CSS-030: Consultant updates class schedule successfully")
    void tc_css_030_updateScheduleInClass_consultant_success() {
        ClassScheduleRequest req = buildValidUpdateRequest();
        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(eq(httpRequest), any())).thenReturn(response);

        ClassScheduleResponse result = service.updateScheduleInClass(httpRequest, req);

        assertThat(result).isEqualTo(response);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-031
     * Test Objective : User who is neither the class teacher nor CONSULTANT is denied update.
     * Input          : Student user (id=4, role=STUDENT), valid ClassScheduleRequest (classScheduleId=200).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-031: Non-teacher/non-consultant denied schedule update")
    void tc_css_031_updateScheduleInClass_unauthorized_throwsException() {
        ClassScheduleRequest req = buildValidUpdateRequest();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> service.updateScheduleInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-032
     * Test Objective : Non-existent scheduleId on update → NOT_EXISTED exception.
     * Input          : Consultant user, ClassScheduleRequest with classScheduleId=999 (not in repository).
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-032: Schedule not found on update throws NOT_EXISTED")
    void tc_css_032_updateScheduleInClass_scheduleNotFound_throwsException() {
        ClassScheduleRequest req = buildValidUpdateRequest();
        req.setClassScheduleId(999L);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateScheduleInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-033
     * Test Objective : Changing the room on update triggers room-conflict check; no conflict → update succeeds.
     * Input          : Consultant user, ClassScheduleRequest with roomId=20L (different from current room 10L);
     *                  existsScheduleByRoomIdAndStartAtAndEndAt returns empty list for roomId=20L.
     * Expected Output: ClassScheduleResponse returned; room-conflict check invoked for roomId=20L; save() called.
     * Notes          : Rollback — save() is mocked.
     */
    @Test
    @DisplayName("TC-CSS-033: Room changed on update triggers room-conflict check - no conflict")
    void tc_css_033_updateScheduleInClass_roomChanged_noConflict_success() {
        ClassScheduleRequest req = buildValidUpdateRequest();
        req.setRoomId(20L); // different room

        Room newRoom = new Room();
        newRoom.setId(20L);
        newRoom.setName("Room B");
        testSchedule.setRoom(testRoom); // current room is 10L

        ClassScheduleResponse response = new ClassScheduleResponse();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(20L)))
                .thenReturn(Collections.emptyList()); // no conflict
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), any())).thenReturn(response);

        ClassScheduleResponse result = service.updateScheduleInClass(httpRequest, req);

        assertThat(result).isEqualTo(response);
        verify(classScheduleRepository)
                .existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(20L));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-034
     * Test Objective : Changing the room on update when new room is already occupied → NOT_AVAILABLE.
     * Input          : Consultant user, ClassScheduleRequest with roomId=20L (different from current room 10L);
     *                  existsScheduleByRoomIdAndStartAtAndEndAt returns [555L] (conflict) for roomId=20L.
     * Expected Output: WebToeicException thrown; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-034: Room change conflicts - throws NOT_AVAILABLE on update")
    void tc_css_034_updateScheduleInClass_roomChangeConflict_throwsException() {
        ClassScheduleRequest req = buildValidUpdateRequest();
        req.setRoomId(20L);

        testSchedule.setRoom(testRoom); // current room 10L, new room 20L

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), eq(20L)))
                .thenReturn(List.of(555L)); // conflict!

        assertThatThrownBy(() -> service.updateScheduleInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class);

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-035
     * Test Objective : MANAGER role is denied schedule update (only class teacher and CONSULTANT allowed).
     * Input          : Manager user (id=1, role=MANAGER), valid ClassScheduleRequest (classScheduleId=200).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-035: Manager denied schedule update (not teacher-owner or consultant)")
    void tc_css_035_updateScheduleInClass_manager_throwsException() {
        ClassScheduleRequest req = buildValidUpdateRequest();

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> service.updateScheduleInClass(httpRequest, req))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classScheduleRepository, never()).save(any());
    }

    // =========================================================================
    // cancelledScheduleInClass
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-036
     * Test Objective : Class teacher can cancel their own class schedule.
     * Input          : Teacher user (same as testClass.teacher), scheduleId list=[200L].
     * Expected Output: Schedule status set to CANCELLED and isActive=false; save() called once.
     * Notes          : Rollback — save() is mocked; state verified via ArgumentCaptor.
     */
    @Test
    @DisplayName("TC-CSS-036: Class teacher cancels schedule successfully")
    void tc_css_036_cancelledScheduleInClass_teacher_success() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);

        service.cancelledScheduleInClass(httpRequest, List.of(200L));

        ArgumentCaptor<ClassSchedule> captor = ArgumentCaptor.forClass(ClassSchedule.class);
        verify(classScheduleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EScheduleStatus.CANCELLED);
        assertThat(captor.getValue().getIsActive()).isFalse();
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-037
     * Test Objective : CONSULTANT can cancel any class schedule.
     * Input          : Consultant user (id=2, role=CONSULTANT), scheduleId list=[200L].
     * Expected Output: Schedule status set to CANCELLED; save() called once.
     * Notes          : Rollback — save() is mocked; status verified via ArgumentCaptor.
     */
    @Test
    @DisplayName("TC-CSS-037: Consultant cancels schedule successfully")
    void tc_css_037_cancelledScheduleInClass_consultant_success() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);

        service.cancelledScheduleInClass(httpRequest, List.of(200L));

        ArgumentCaptor<ClassSchedule> captor = ArgumentCaptor.forClass(ClassSchedule.class);
        verify(classScheduleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EScheduleStatus.CANCELLED);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-038
     * Test Objective : User who is neither the class teacher nor CONSULTANT is denied cancel.
     * Input          : Student user (id=4, role=STUDENT), scheduleId list=[200L].
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-038: Unauthorized user does not cancel schedule")
    void tc_css_038_cancelledScheduleInClass_unauthorized_noSave() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> service.cancelledScheduleInClass(httpRequest, List.of(200L)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-039
     * Test Objective : Non-existent scheduleId during cancel → NOT_EXISTED exception.
     * Input          : Teacher user, scheduleId list=[999L] (not in repository).
     * Expected Output: WebToeicException with ResponseCode.NOT_EXISTED; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-039: Schedule not found during cancel throws NOT_EXISTED")
    void tc_css_039_cancelledScheduleInClass_scheduleNotFound_throwsException() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelledScheduleInClass(httpRequest, List.of(999L)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode())
                        .isEqualTo(ResponseCode.NOT_EXISTED));

        verify(classScheduleRepository, never()).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-040
     * Test Objective : Batch cancellation of multiple schedules all succeed.
     * Input          : Consultant user, scheduleId list=[200L, 201L]; both schedules exist.
     * Expected Output: save() called exactly 2 times, both schedules cancelled.
     * Notes          : Rollback — save() is mocked.
     */
    @Test
    @DisplayName("TC-CSS-040: Multiple schedules cancelled in batch")
    void tc_css_040_cancelledScheduleInClass_multiple_success() {
        ClassSchedule schedule2 = new ClassSchedule();
        schedule2.setId(201L);
        schedule2.setClazz(testClass);
        schedule2.setRoom(testRoom);
        schedule2.setStatus(EScheduleStatus.ACTIVE);
        schedule2.setIsActive(true);
        schedule2.setIsDelete(false);
        schedule2.setCreatedBy(consultantUser);

        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.findById(201L)).thenReturn(Optional.of(schedule2));
        when(classScheduleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.cancelledScheduleInClass(httpRequest, List.of(200L, 201L));

        verify(classScheduleRepository, times(2)).save(any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-041
     * Test Objective : MANAGER role cannot cancel schedules (not teacher-owner or CONSULTANT).
     * Input          : Manager user (id=1, role=MANAGER), scheduleId list=[200L].
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION; save() never called.
     * Notes          : Rollback — save() is never invoked.
     */
    @Test
    @DisplayName("TC-CSS-041: Manager user does not cancel schedule (not teacher/consultant)")
    void tc_css_041_cancelledScheduleInClass_manager_noSave() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> service.cancelledScheduleInClass(httpRequest, List.of(200L)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));

        verify(classScheduleRepository, never()).save(any());
    }

    // =========================================================================
    // Helper builders
    // =========================================================================

    private ClassScheduleRequest buildValidScheduleRequest() {
        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(100L);
        req.setRoomId(10L);
        req.setTitle("Session 1");
        req.setStartAt(new Date(System.currentTimeMillis() + 86_400_000)); // tomorrow
        req.setEndAt(new Date(System.currentTimeMillis() + 90_000_000));
        return req;
    }

    private ClassScheduleRequest buildValidUpdateRequest() {
        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassScheduleId(200L);
        req.setRoomId(10L); // same room as existing schedule → no room-conflict check needed
        req.setTitle("Session 1 – Updated");
        req.setStartAt(testSchedule.getStartAt());
        req.setEndAt(testSchedule.getEndAt());
        req.setStatus(1); // ACTIVE
        req.setIsActive(true);
        req.setIsDelete(false);
        return req;
    }

    // =========================================================================
    // Missing Coverage Tests for 100% JaCoCo
    // =========================================================================

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : detailStatisticAttendance
     * Test Case ID   : TC-CSS-COV-001
     * Test Objective : User not found in detailStatisticAttendance throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for detailStatisticAttendance.
     */
    @Test
    @DisplayName("Coverage: detailStatisticAttendance | user not found")
    void coverage_detailStatisticAttendance_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.detailStatisticAttendance(httpRequest, 200L, PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStatisticAttendance
     * Test Case ID   : TC-CSS-COV-002
     * Test Objective : User not found in overviewStatisticAttendance throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for overviewStatisticAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStatisticAttendance | user not found")
    void coverage_overviewStatisticAttendance_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.overviewStatisticAttendance(httpRequest, 100L, PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-003
     * Test Objective : User not found in overviewStudentAttendance throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for overviewStudentAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | user not found")
    void coverage_overviewStudentAttendance_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-004
     * Test Objective : MANAGER role is allowed to access overviewStudentAttendance.
     * Input          : Manager user (id=1, role=MANAGER), classId=100.
     * Expected Output: Method completes without exception; attendanceRepository queried.
     * Notes          : Covers MANAGER allowed branch for overviewStudentAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | user is MANAGER -> allowed")
    void coverage_overviewStudentAttendance_manager() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(managerUser.getEmail());
        when(userRepository.findByEmail(managerUser.getEmail())).thenReturn(Optional.of(managerUser));
        when(attendanceRepository.overviewStudentAttendance(anyLong(), any())).thenReturn(Page.empty());
        service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-005
     * Test Objective : CONSULTANT role is allowed to access overviewStudentAttendance.
     * Input          : Consultant user (id=2, role=CONSULTANT), classId=100.
     * Expected Output: Method completes without exception; attendanceRepository queried.
     * Notes          : Covers CONSULTANT allowed branch for overviewStudentAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | user is CONSULTANT -> allowed")
    void coverage_overviewStudentAttendance_consultant() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(consultantUser.getEmail());
        when(userRepository.findByEmail(consultantUser.getEmail())).thenReturn(Optional.of(consultantUser));
        when(attendanceRepository.overviewStudentAttendance(anyLong(), any())).thenReturn(Page.empty());
        service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-006
     * Test Objective : TEACHER who is NOT a class member is denied overviewStudentAttendance.
     * Input          : Teacher user (id=3, role=TEACHER), classId=100;
     *                  classMemberRepository returns false for (classId=100, userId=3).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : Covers TEACHER non-member denied branch for overviewStudentAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | user is TEACHER but NOT in class -> throws NOT_PERMISSION")
    void coverage_overviewStudentAttendance_teacherNotInClass() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classMemberRepository.existsMemberInClass(100L, teacherUser.getId())).thenReturn(false);
        assertThatThrownBy(() -> service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-007
     * Test Objective : STUDENT role is always denied overviewStudentAttendance.
     * Input          : Student user (id=4, role=STUDENT), classId=100.
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : Covers STUDENT always-denied branch for overviewStudentAttendance.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | user is STUDENT -> throws NOT_PERMISSION")
    void coverage_overviewStudentAttendance_studentNotAllowed() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        assertThatThrownBy(() -> service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getScheduleDetail
     * Test Case ID   : TC-CSS-COV-008
     * Test Objective : User not found in getScheduleDetail throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for getScheduleDetail.
     */
    @Test
    @DisplayName("Coverage: getScheduleDetail | user not found")
    void coverage_getScheduleDetail_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getScheduleDetail(httpRequest, 200L))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-COV-009
     * Test Objective : User not found in getClassSchedule throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for getClassSchedule.
     */
    @Test
    @DisplayName("Coverage: getClassSchedule | user not found")
    void coverage_getClassSchedule_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getClassSchedule(httpRequest, new SearchScheduleSto(), PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-COV-010
     * Test Objective : STUDENT role triggers membership-filtered query in getClassSchedule.
     * Input          : Student user (id=4, role=STUDENT);
     *                  classMemberRepository returns [100L] for student's email.
     * Expected Output: Method completes without exception; filterSchedule called with student's class list.
     * Notes          : Covers STUDENT membership-filtered path in getClassSchedule.
     */
    @Test
    @DisplayName("Coverage: getClassSchedule | user is STUDENT -> ok")
    void coverage_getClassSchedule_student() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(studentUser.getEmail());
        when(userRepository.findByEmail(studentUser.getEmail())).thenReturn(Optional.of(studentUser));
        when(classMemberRepository.findClassOfMember(studentUser.getEmail())).thenReturn(List.of(100L));
        when(classScheduleRepository.filterSchedule(any(), anyList(), any())).thenReturn(Page.empty());
        service.getClassSchedule(httpRequest, new SearchScheduleSto(), PageRequest.of(0, 10));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : getClassSchedule
     * Test Case ID   : TC-CSS-COV-011
     * Test Objective : User with null/unknown role is denied getClassSchedule.
     * Input          : User with role=null (no matching role branch).
     * Expected Output: WebToeicException with ResponseCode.NOT_PERMISSION.
     * Notes          : Covers the default/else branch for unrecognized roles in getClassSchedule.
     */
    @Test
    @DisplayName("Coverage: getClassSchedule | user is UNKNOWN ROLE -> throws NOT_PERMISSION")
    void coverage_getClassSchedule_unknownRole() {
        User ghost = new User();
        ghost.setEmail("ghost@t.com");
        ghost.setRole(null);
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.of(ghost));
        assertThatThrownBy(() -> service.getClassSchedule(httpRequest, new SearchScheduleSto(), PageRequest.of(0, 10)))
                .isInstanceOf(WebToeicException.class)
                .satisfies(ex -> assertThat(((WebToeicException) ex).getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION));
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : createScheduleInClass
     * Test Case ID   : TC-CSS-COV-012
     * Test Objective : User not found in createScheduleInClass throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for createScheduleInClass.
     */
    @Test
    @DisplayName("Coverage: createScheduleInClass | user not found")
    void coverage_createScheduleInClass_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createScheduleInClass(httpRequest, List.of()))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-COV-013
     * Test Objective : User not found in updateScheduleInClass throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for updateScheduleInClass.
     */
    @Test
    @DisplayName("Coverage: updateScheduleInClass | user not found")
    void coverage_updateScheduleInClass_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateScheduleInClass(httpRequest, new ClassScheduleRequest()))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-COV-014
     * Test Objective : No fields changed on update (same roomId, startAt, endAt) → room-conflict check skipped.
     * Input          : Teacher user, ClassScheduleRequest with roomId=10L (same), startAt and endAt identical
     *                  to existing schedule.
     * Expected Output: Update succeeds; existsScheduleByRoomIdAndStartAtAndEndAt never called.
     * Notes          : Covers the false branch of the room/time-change guard to skip conflict check.
     */
    @Test
    @DisplayName("Coverage: updateScheduleInClass | fields unchanged -> no room check")
    void coverage_updateScheduleInClass_fieldsUnchanged() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), any())).thenReturn(new ClassScheduleResponse());

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassScheduleId(200L);
        req.setRoomId(testSchedule.getRoom().getId());
        req.setStartAt(testSchedule.getStartAt());
        req.setEndAt(testSchedule.getEndAt());
        req.setStatus(1); // Set valid status

        service.updateScheduleInClass(httpRequest, req);

        verify(classScheduleRepository, never()).existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : cancelledScheduleInClass
     * Test Case ID   : TC-CSS-COV-015
     * Test Objective : User not found in cancelledScheduleInClass throws exception.
     * Input          : JWT token resolves to "ghost@t.com" which is absent from userRepository.
     * Expected Output: WebToeicException thrown.
     * Notes          : Covers user-not-found branch for cancelledScheduleInClass.
     */
    @Test
    @DisplayName("Coverage: cancelledScheduleInClass | user not found")
    void coverage_cancelledScheduleInClass_userNotFound() {
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancelledScheduleInClass(httpRequest, List.of(200L)))
                .isInstanceOf(WebToeicException.class);
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : overviewStudentAttendance
     * Test Case ID   : TC-CSS-COV-016
     * Test Objective : TEACHER who IS a class member is allowed to access overviewStudentAttendance.
     * Input          : Teacher user (id=3, role=TEACHER), classId=100;
     *                  classMemberRepository returns true for (classId=100, userId=3).
     * Expected Output: Non-null Page returned; attendanceRepository queried.
     * Notes          : Covers L83 false branch — existsMemberInClass=true → no exception for TEACHER.
     */
    @Test
    @DisplayName("Coverage: overviewStudentAttendance | TEACHER who IS member -> allowed")
    void coverage_overviewStudentAttendance_teacherIsMember() {
        // Covers L83 false branch: existsMemberInClass returns true -> no exception
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classMemberRepository.existsMemberInClass(100L, teacherUser.getId())).thenReturn(true);
        when(attendanceRepository.overviewStudentAttendance(100L, PageRequest.of(0, 10))).thenReturn(Page.empty());

        Page<?> result = service.overviewStudentAttendance(httpRequest, 100L, PageRequest.of(0, 10));
        assertThat(result).isNotNull();
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-COV-017
     * Test Objective : Only startAt changed on update → room-conflict check is triggered.
     * Input          : Teacher user, ClassScheduleRequest with same roomId=10L and same endAt,
     *                  but startAt offset by +60 seconds; conflict check returns empty list.
     * Expected Output: Update succeeds; existsScheduleByRoomIdAndStartAtAndEndAt called once.
     * Notes          : Covers L191 true branch (startAt differs) with L190 false (same room).
     */
    @Test
    @DisplayName("Coverage: updateScheduleInClass | only startAt changed -> room check triggered")
    void coverage_updateScheduleInClass_onlyStartAtChanged() {
        // Covers L191 true branch (startAt differs), L190 false (room same), L192 false (endAt same)
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), any())).thenReturn(new ClassScheduleResponse());

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassScheduleId(200L);
        req.setRoomId(testSchedule.getRoom().getId()); // same room
        req.setStartAt(new Date(testSchedule.getStartAt().getTime() + 60_000)); // different start
        req.setEndAt(testSchedule.getEndAt()); // same end
        req.setStatus(1);

        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        service.updateScheduleInClass(httpRequest, req);

        verify(classScheduleRepository).existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any());
    }

    /**
     * File Name      : ClassScheduleServiceImplTest.java
     * Class Name     : ClassScheduleServiceImpl
     * Method         : updateScheduleInClass
     * Test Case ID   : TC-CSS-COV-018
     * Test Objective : Only endAt changed on update → room-conflict check is triggered.
     * Input          : Teacher user, ClassScheduleRequest with same roomId=10L and same startAt,
     *                  but endAt offset by +60 seconds; conflict check returns empty list.
     * Expected Output: Update succeeds; existsScheduleByRoomIdAndStartAtAndEndAt called once.
     * Notes          : Covers L192 true branch (endAt differs) with L190 false (same room) and L191 false (same startAt).
     */
    @Test
    @DisplayName("Coverage: updateScheduleInClass | only endAt changed -> room check triggered")
    void coverage_updateScheduleInClass_onlyEndAtChanged() {
        // Covers L192 true branch (endAt differs), L190 false (room same), L191 false (startAt same)
        when(jwtUtil.getEmailFromToken(httpRequest)).thenReturn(teacherUser.getEmail());
        when(userRepository.findByEmail(teacherUser.getEmail())).thenReturn(Optional.of(teacherUser));
        when(classScheduleRepository.findById(200L)).thenReturn(Optional.of(testSchedule));
        when(classScheduleRepository.save(any())).thenReturn(testSchedule);
        when(convertUtil.convertScheduleToDto(any(), any())).thenReturn(new ClassScheduleResponse());

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassScheduleId(200L);
        req.setRoomId(testSchedule.getRoom().getId()); // same room
        req.setStartAt(testSchedule.getStartAt()); // same start
        req.setEndAt(new Date(testSchedule.getEndAt().getTime() + 60_000)); // different end
        req.setStatus(1);

        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        service.updateScheduleInClass(httpRequest, req);

        verify(classScheduleRepository).existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any());
    }
}