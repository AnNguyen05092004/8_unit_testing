package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Attendance;
import com.doan2025.webtoeic.domain.ClassSchedule;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.request.AttendanceRequest;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttendanceRepository;
import com.doan2025.webtoeic.repository.ClassScheduleRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private ClassScheduleRepository classScheduleRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAttendance_timeWindowCases")
    void updateAttendance_shouldValidateTimeWindow(
            String name,
            Date startAt,
            Date endAt,
            ResponseCode expectedCode
    ) {
        User teacher = teacher("teacher@gmail.com");
        ClassSchedule schedule = scheduleWithTeacher(startAt, endAt, teacher);

        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(1L);
        request.setAttendanceId(10L);
        request.setAttendanceStatus(1);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(ResponseObject.SCHEDULE, ex.getResponseObject());
    }

    private static Stream<Arguments> updateAttendance_timeWindowCases() {
        return Stream.of(
                Arguments.of(
                        "Before window -> NOT_START",
                        Date.from(Instant.now().plus(40, ChronoUnit.MINUTES)),
                        Date.from(Instant.now().plus(100, ChronoUnit.MINUTES)),
                        ResponseCode.NOT_START
                ),
                Arguments.of(
                        "After window -> OVER_DUE",
                        Date.from(Instant.now().minus(120, ChronoUnit.MINUTES)),
                        Date.from(Instant.now().minus(30, ChronoUnit.MINUTES)),
                        ResponseCode.OVER_DUE
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAttendance_scheduleIdCases")
    void updateAttendance_shouldValidateScheduleId(
            String name,
            Long scheduleId,
            Optional<ClassSchedule> scheduleOpt,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        User teacher = teacher("teacher@gmail.com");
        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(scheduleId);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));

        if (scheduleId != null) {
            when(classScheduleRepository.findById(scheduleId)).thenReturn(scheduleOpt);
        }

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> updateAttendance_scheduleIdCases() {
        return Stream.of(
                Arguments.of(
                        "Null scheduleId -> IS_NULL",
                        null,
                        Optional.empty(),
                        ResponseCode.IS_NULL,
                        ResponseObject.SCHEDULE
                ),
                Arguments.of(
                        "Not existed schedule -> NOT_EXISTED",
                        999L,
                        Optional.empty(),
                        ResponseCode.NOT_EXISTED,
                        ResponseObject.SCHEDULE
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAttendance_callerCases")
    void updateAttendance_shouldValidateCaller(
            String name,
            String email,
            Optional<User> userOpt,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(userOpt);

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> updateAttendance_callerCases() {
        return Stream.of(
                Arguments.of(
                        "Caller not found -> NOT_EXISTED",
                        "missing@gmail.com",
                        Optional.empty(),
                        ResponseCode.NOT_EXISTED,
                        ResponseObject.USER
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateAttendance_permissionCases")
    void updateAttendance_shouldValidatePermission(
            String name,
            User caller,
            User owner,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)),
                owner
        );

        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn(caller.getEmail());
        when(userRepository.findByEmail(caller.getEmail())).thenReturn(Optional.of(caller));
        when(classScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> updateAttendance_permissionCases() {
        return Stream.of(
                Arguments.of(
                        "Student caller -> NOT_PERMISSION",
                        user("student@gmail.com", ERole.STUDENT),
                        user("owner@gmail.com", ERole.TEACHER),
                        ResponseCode.NOT_PERMISSION,
                        ResponseObject.USER
                )
        );
    }

    @Test
    void updateAttendance_shouldThrowNotExistedAttendance_whenAttendanceIdNotFound() {
        User teacher = teacher("teacher@gmail.com");
        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)),
                teacher
        );

        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(1L);
        request.setAttendanceId(999L);
        request.setAttendanceStatus(1);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );
        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.ATTENDANCE, ex.getResponseObject());
    }

    @Test
    void attendance_shouldCreateAttendances_whenInputIsValid() {
        User teacher = teacher("teacher@gmail.com");
        User student = new User();
        student.setId(6L);
        student.setEmail("student@gmail.com");

        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(50, ChronoUnit.MINUTES)),
                teacher
        );
        schedule.setId(5L);
        schedule.setIsAttendance(false);

        AttendanceRequest request = new AttendanceRequest();
        request.setClassId(1L);
        request.setStudentId(6L);
        request.setAttendanceStatus(1);
        request.setCheckIn(new Date());

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(5L));
        when(classScheduleRepository.findAllById(List.of(5L))).thenReturn(new ArrayList<>(List.of(schedule)));
        when(attendanceRepository.findByScheduleId(5L)).thenReturn(null);
        when(classScheduleRepository.findById(5L)).thenReturn(Optional.of(schedule));
        when(userRepository.findById(6L)).thenReturn(Optional.of(student));
        when(attendanceRepository.saveAll(anyList())).thenReturn(List.of(new Attendance()));

        attendanceService.attendance(httpServletRequest, List.of(request));

        verify(attendanceRepository).saveAll(anyList());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attendance_availabilityCases")
    void attendance_shouldValidateAvailability(
            String name,
            List<Long> availableScheduleIds,
            List<Long> existedAttendanceIds,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        User teacher = teacher("teacher@gmail.com");
        AttendanceRequest request = new AttendanceRequest();
        request.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(availableScheduleIds);

        if (availableScheduleIds != null) {
            ClassSchedule schedule = scheduleWithTeacher(
                    Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)),
                    Date.from(Instant.now().plus(50, ChronoUnit.MINUTES)),
                    teacher
            );
            schedule.setId(5L);

            when(classScheduleRepository.findAllById(availableScheduleIds))
                    .thenReturn(new ArrayList<>(List.of(schedule)));
            when(attendanceRepository.findByScheduleId(5L)).thenReturn(existedAttendanceIds);
        }

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.attendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> attendance_availabilityCases() {
        return Stream.of(
                Arguments.of(
                        "No available schedule -> NOT_AVAILABLE",
                        null,
                        null,
                        ResponseCode.NOT_AVAILABLE,
                        ResponseObject.ATTENDANCE
                ),
                Arguments.of(
                        "Attendance already existed -> EXISTED",
                        List.of(5L),
                        List.of(1L),
                        ResponseCode.EXISTED,
                        ResponseObject.ATTENDANCE
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attendance_permissionAndStudentCases")
    void attendance_shouldValidatePermissionAndStudent(
            String name,
            User caller,
            User owner,
            Long studentId,
            Optional<User> studentOpt,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(50, ChronoUnit.MINUTES)),
                owner
        );
        schedule.setId(5L);
        schedule.setIsAttendance(false);

        AttendanceRequest request = new AttendanceRequest();
        request.setClassId(1L);
        request.setStudentId(studentId);
        request.setAttendanceStatus(1);
        request.setCheckIn(new Date());

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn(caller.getEmail());
        when(userRepository.findByEmail(caller.getEmail())).thenReturn(Optional.of(caller));
        when(classScheduleRepository.getAvailableSchedule(1L)).thenReturn(List.of(5L));
        when(classScheduleRepository.findAllById(List.of(5L))).thenReturn(new ArrayList<>(List.of(schedule)));
        when(attendanceRepository.findByScheduleId(5L)).thenReturn(null);
        when(classScheduleRepository.findById(5L)).thenReturn(Optional.of(schedule));

        if (studentId != null) {
            when(userRepository.findById(studentId)).thenReturn(studentOpt);
        }

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.attendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> attendance_permissionAndStudentCases() {
        return Stream.of(
                Arguments.of(
                        "Teacher is not owner -> NOT_PERMISSION",
                        user("caller@gmail.com", ERole.TEACHER),
                        user("owner@gmail.com", ERole.TEACHER),
                        null,
                        Optional.empty(),
                        ResponseCode.NOT_PERMISSION,
                        ResponseObject.USER
                ),
                Arguments.of(
                        "Student not found -> NOT_EXISTED",
                        user("teacher@gmail.com", ERole.TEACHER),
                        user("teacher@gmail.com", ERole.TEACHER),
                        999L,
                        Optional.empty(),
                        ResponseCode.NOT_EXISTED,
                        ResponseObject.USER
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("attendance_callerCases")
    void attendance_shouldValidateCaller(
            String name,
            String email,
            Optional<User> userOpt,
            ResponseCode expectedCode,
            ResponseObject expectedObject
    ) {
        AttendanceRequest request = new AttendanceRequest();
        request.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(userOpt);

        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> attendanceService.attendance(httpServletRequest, List.of(request))
        );

        assertEquals(expectedCode, ex.getResponseCode());
        assertEquals(expectedObject, ex.getResponseObject());
    }

    private static Stream<Arguments> attendance_callerCases() {
        return Stream.of(
                Arguments.of(
                        "Caller not found -> NOT_EXISTED",
                        "missing@gmail.com",
                        Optional.empty(),
                        ResponseCode.NOT_EXISTED,
                        ResponseObject.USER
                )
        );
    }

    private User teacher(String email) {
        User teacher = new User();
        teacher.setEmail(email);
        teacher.setRole(ERole.TEACHER);
        return teacher;
    }

    private static User user(String email, ERole role) {
        User u = new User();
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private ClassSchedule scheduleWithTeacher(Date startAt, Date endAt, User teacher) {
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .teacher(teacher)
                .build();
        return ClassSchedule.builder()
                .id(1L)
                .clazz(clazz)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    @Test
    void updateAttendance_shouldThrowException_whenRequestListIsNull() {
        // Arrange: null attendance request list (invalid data type).
        User teacher = teacher("teacher@gmail.com");
        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)),
                teacher
        );

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));

        // Act + Assert
        assertThrows(
                Exception.class,  // NullPointerException expected
                () -> attendanceService.updateAttendance(httpServletRequest, null)  // Invalid: null list
        );
    }

    @Test
    void attendance_shouldThrowException_whenRequestListIsNull() {
        // Arrange: null attendance request for creation (invalid data type).
        User teacher = teacher("teacher@gmail.com");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));

        // Act + Assert
        assertThrows(
                Exception.class,  // NullPointerException expected
                () -> attendanceService.attendance(httpServletRequest, null)  // Invalid: null list
        );
    }

    @Test
    void updateAttendance_shouldThrowException_whenAttendanceIdIsNull() {
        // Arrange: attendance request with null ID (invalid data type).
        User teacher = teacher("teacher@gmail.com");
        ClassSchedule schedule = scheduleWithTeacher(
                Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)),
                Date.from(Instant.now().plus(20, ChronoUnit.MINUTES)),
                teacher
        );

        AttendanceRequest request = new AttendanceRequest();
        request.setScheduleId(1L);
        request.setAttendanceId(null);  // Invalid: null ID
        request.setAttendanceStatus(1);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classScheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        // Act + Assert
        assertThrows(
                Exception.class,  // NullPointerException expected
                () -> attendanceService.updateAttendance(httpServletRequest, List.of(request))
        );
    }
}
