package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.EScheduleStatus;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ClassSchedule;
import com.doan2025.webtoeic.domain.Room;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchScheduleSto;
import com.doan2025.webtoeic.dto.request.ClassScheduleRequest;
import com.doan2025.webtoeic.dto.response.ClassScheduleResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttendanceRepository;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassRepository;
import com.doan2025.webtoeic.repository.ClassScheduleRepository;
import com.doan2025.webtoeic.repository.RoomRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassScheduleServiceImplTest {

    @Mock
    private ClassScheduleRepository classScheduleRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private ClassMemberRepository classMemberRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ClassScheduleServiceImpl classScheduleService;

    @Test
    void createScheduleInClass_shouldThrowIsNull_whenRoomIdIsNull() {
                // Arrange: room id is missing in the request.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .build();

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(1L);
        req.setRoomId(null);
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
        );

        assertEquals(ResponseCode.IS_NULL, ex.getResponseCode());
        assertEquals(ResponseObject.ROOM, ex.getResponseObject());
    }

    @Test
    void createScheduleInClass_shouldThrowNotAvailable_whenRoomIsOverlapped() {
                // Arrange: overlapping room schedule is detected.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .build();

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(1L);
        req.setRoomId(10L);
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of(99L));

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
        );

        assertEquals(ResponseCode.NOT_AVAILABLE, ex.getResponseCode());
        assertEquals(ResponseObject.ROOM, ex.getResponseObject());
    }

    @Test
    void createScheduleInClass_shouldCreateSchedule_whenInputIsValid() {
                // Arrange: all dependencies return valid data for schedule creation.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .build();

        Room room = Room.builder().id(10L).build();

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(1L);
        req.setRoomId(10L);
        req.setTitle("Morning Shift");
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        ClassSchedule saved = ClassSchedule.builder()
                .id(100L)
                .clazz(clazz)
                .room(room)
                .status(EScheduleStatus.ACTIVE)
                .title(req.getTitle())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .build();

        ClassScheduleResponse response = ClassScheduleResponse.builder().id(100L).title("Morning Shift").build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(classScheduleRepository.save(any(ClassSchedule.class))).thenReturn(saved);
        when(convertUtil.convertScheduleToDto(httpServletRequest, saved)).thenReturn(response);

                // Act
        List<?> result = classScheduleService.createScheduleInClass(httpServletRequest, List.of(req));

                // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof ClassScheduleResponse);
        verify(classScheduleRepository).save(any(ClassSchedule.class));
    }

    @Test
    void createScheduleInClass_shouldThrowNotExistedClass_whenClassNotFound() {
        // Arrange: authenticated user but class id does not exist.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(999L);
        req.setRoomId(10L);
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.CLASS, ex.getResponseObject());
    }

    @Test
    void createScheduleInClass_shouldThrowNotExistedRoom_whenRoomNotFound() {
        // Arrange: room id is provided but room does not exist.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .build();

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(1L);
        req.setRoomId(88L);
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());
        when(roomRepository.findById(88L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.ROOM, ex.getResponseObject());
    }

    @Test
    void createScheduleInClass_shouldThrowNotAvailable_whenClassHasOverlappedSchedule() {
        // Arrange: class already has schedule in requested time slot.
        User consultant = new User();
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .build();

        ClassScheduleRequest req = new ClassScheduleRequest();
        req.setClassId(1L);
        req.setRoomId(10L);
        req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
        req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of());
        when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), any()))
                .thenReturn(List.of(7L));

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
        );

        assertEquals(ResponseCode.NOT_AVAILABLE, ex.getResponseCode());
        assertEquals(ResponseObject.SCHEDULE, ex.getResponseObject());
    }

        @Test
        void createScheduleInClass_shouldThrowNotExistedUser_whenCallerNotFound() {
                // Arrange: token email does not map to a user.
                ClassScheduleRequest req = new ClassScheduleRequest();
                req.setClassId(1L);
                req.setRoomId(10L);
                req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
                req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("missing@gmail.com");
                when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

                // Act + Assert
                WebToeicException ex = assertThrows(
                                WebToeicException.class,
                                () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
                );

                assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
                assertEquals(ResponseObject.USER, ex.getResponseObject());
        }

        @Test
        void createScheduleInClass_shouldHandleZeroRoomId() {
                // Arrange: edge case with zero room ID.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                        .id(1L)
                        .build();

                ClassScheduleRequest req = new ClassScheduleRequest();
                req.setClassId(1L);
                req.setRoomId(0L);  // Edge case: zero ID
                req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
                req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
                when(roomRepository.findById(0L)).thenReturn(Optional.empty());

                // Act + Assert
                WebToeicException ex = assertThrows(
                        WebToeicException.class,
                        () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
                );

                assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
                assertEquals(ResponseObject.ROOM, ex.getResponseObject());
        }

        @Test
        void createScheduleInClass_shouldHandleLargeRoomId() {
                // Arrange: edge case with very large room ID (near Long.MAX_VALUE).
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                        .id(1L)
                        .build();

                ClassScheduleRequest req = new ClassScheduleRequest();
                req.setClassId(1L);
                req.setRoomId(Long.MAX_VALUE - 1);  // Boundary: very large ID
                req.setStartAt(new Date(System.currentTimeMillis() + 60_000));
                req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
                when(classScheduleRepository.existsScheduleByRoomIdAndStartAtAndEndAt(any(), any(), any()))
                        .thenReturn(List.of());
                when(classScheduleRepository.existsScheduleByClassIdAndStartAtAndEndAt(any(), any(), any()))
                        .thenReturn(List.of());
                when(roomRepository.findById(Long.MAX_VALUE - 1)).thenReturn(Optional.empty());

                // Act + Assert
                WebToeicException ex = assertThrows(
                        WebToeicException.class,
                        () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
                );

                assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
                assertEquals(ResponseObject.ROOM, ex.getResponseObject());
        }

        @Test
        void createScheduleInClass_shouldThrowException_whenStartDateIsNull() {
                // Arrange: schedule request with null start date (invalid data type).
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                        .id(1L)
                        .build();

                ClassScheduleRequest req = new ClassScheduleRequest();
                req.setClassId(1L);
                req.setRoomId(10L);
                req.setStartAt(null);  // Invalid: null start date
                req.setEndAt(new Date(System.currentTimeMillis() + 120_000));

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));

                // Act + Assert
                assertThrows(
                        Exception.class,
                        () -> classScheduleService.createScheduleInClass(httpServletRequest, List.of(req))
                );
        }

        @Test
        void updateScheduleInClass_shouldUpdateSchedule_whenCallerIsConsultant() {
                // Arrange: consultant can update an existing schedule.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                User teacher = new User();
                teacher.setEmail("teacher@gmail.com");
                teacher.setRole(ERole.TEACHER);

                com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                        .id(1L)
                        .teacher(teacher)
                        .build();

                Room room = Room.builder().id(10L).build();
                ClassSchedule schedule = ClassSchedule.builder()
                        .id(100L)
                        .clazz(clazz)
                        .room(room)
                        .status(EScheduleStatus.ACTIVE)
                        .title("Old title")
                        .startAt(new Date(System.currentTimeMillis() + 60_000))
                        .endAt(new Date(System.currentTimeMillis() + 120_000))
                        .build();

                ClassScheduleRequest request = new ClassScheduleRequest();
                request.setClassScheduleId(100L);
                request.setTitle("New title");
                request.setStartAt(schedule.getStartAt());
                request.setEndAt(schedule.getEndAt());
                request.setStatus(1);
                request.setIsActive(true);
                request.setIsDelete(false);
                request.setRoomId(10L);

                ClassScheduleResponse response = ClassScheduleResponse.builder().id(100L).title("New title").build();

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
                when(classScheduleRepository.save(schedule)).thenReturn(schedule);
                when(convertUtil.convertScheduleToDto(httpServletRequest, schedule)).thenReturn(response);

                // Act
                ClassScheduleResponse result = classScheduleService.updateScheduleInClass(httpServletRequest, request);

                // Assert
                assertEquals(100L, result.getId());
                assertEquals("New title", result.getTitle());
        }

        @Test
        void getClassSchedule_shouldReturnTeacherScope_whenCallerIsTeacher() {
                // Arrange: teacher sees schedules in their classes.
                User teacher = new User();
                teacher.setEmail("teacher@gmail.com");
                teacher.setRole(ERole.TEACHER);

                SearchScheduleSto dto = new SearchScheduleSto();
                dto.setClassId(List.of());
                dto.setTeacherId(List.of());
                dto.setStatus(List.of());

                Pageable pageable = PageRequest.of(0, 10);
                ClassSchedule schedule = ClassSchedule.builder().id(100L).build();
                ClassScheduleResponse response = ClassScheduleResponse.builder().id(100L).build();

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
                when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
                when(classMemberRepository.findClassOfMember("teacher@gmail.com")).thenReturn(List.of(1L));
                when(classScheduleRepository.filterSchedule(org.mockito.ArgumentMatchers.any(SearchScheduleSto.class), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.any(Pageable.class)))
                        .thenReturn(new PageImpl<>(List.of(schedule), pageable, 1));
                when(convertUtil.convertScheduleToDto(httpServletRequest, schedule)).thenReturn(response);

                // Act
                Page<?> result = classScheduleService.getClassSchedule(httpServletRequest, dto, pageable);

                // Assert
                assertEquals(1, result.getTotalElements());
        }

        @Test
        void cancelledScheduleInClass_shouldCancelWhenCallerIsConsultant() {
                // Arrange: consultant can cancel schedule.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);
                consultant.setCode(ERole.CONSULTANT.getCode());

                User teacher = new User();
                teacher.setEmail("teacher@gmail.com");
                teacher.setRole(ERole.TEACHER);
                teacher.setCode(ERole.TEACHER.getCode());

                com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                        .teacher(teacher)
                        .build();

                ClassSchedule schedule = ClassSchedule.builder()
                        .id(100L)
                        .clazz(clazz)
                        .status(EScheduleStatus.ACTIVE)
                        .isActive(true)
                        .build();

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));

                // Act
                classScheduleService.cancelledScheduleInClass(httpServletRequest, List.of(100L));

                // Assert
                assertEquals(EScheduleStatus.CANCELLED, schedule.getStatus());
                assertEquals(false, schedule.getIsActive());
        }

        @Test
        void detailStatisticAttendance_shouldReturnPage_whenCallerIsConsultant() {
                // Arrange: consultant can access detail attendance statistic.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                ClassSchedule schedule = ClassSchedule.builder()
                        .id(100L)
                        .clazz(com.doan2025.webtoeic.domain.Class.builder().id(1L).build())
                        .build();

                Pageable pageable = PageRequest.of(0, 10);

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(classScheduleRepository.findById(100L)).thenReturn(Optional.of(schedule));
                when(attendanceRepository.detailStatisticAttendance(100L, pageable)).thenReturn(new PageImpl<>(List.of()));

                // Act
                Page<?> result = classScheduleService.detailStatisticAttendance(httpServletRequest, 100L, pageable);

                // Assert
                assertEquals(0, result.getTotalElements());
        }

        @Test
        void overviewStatisticAttendance_shouldReturnPage_whenCallerIsConsultant() {
                // Arrange: consultant can access overview statistic.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                Pageable pageable = PageRequest.of(0, 10);

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(attendanceRepository.overviewStatisticAttendance(1L, pageable)).thenReturn(new PageImpl<>(List.of()));

                // Act
                Page<?> result = classScheduleService.overviewStatisticAttendance(httpServletRequest, 1L, pageable);

                // Assert
                assertEquals(0, result.getTotalElements());
        }

        @Test
        void overviewStudentAttendance_shouldReturnPage_whenCallerIsConsultant() {
                // Arrange: consultant can access student attendance overview.
                User consultant = new User();
                consultant.setEmail("consultant@gmail.com");
                consultant.setRole(ERole.CONSULTANT);

                Pageable pageable = PageRequest.of(0, 10);

                when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
                when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
                when(attendanceRepository.overviewStudentAttendance(1L, pageable)).thenReturn(new PageImpl<>(List.of()));

                // Act
                Page<?> result = classScheduleService.overviewStudentAttendance(httpServletRequest, 1L, pageable);

                // Assert
                assertEquals(0, result.getTotalElements());
        }
}

