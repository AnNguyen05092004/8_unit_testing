package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ClassNotification;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchNotificationInClassDto;
import com.doan2025.webtoeic.dto.request.ClassNotificationRequest;
import com.doan2025.webtoeic.dto.response.ClassNotificationResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AttachDocumentClassRepository;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassNotificationRepository;
import com.doan2025.webtoeic.repository.ClassRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassNotificationServiceImplTest {

    @Mock
    private ClassRepository classRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private ClassNotificationRepository classNotificationRepository;
    @Mock
    private AttachDocumentClassRepository attachDocumentClassRepository;
    @Mock
    private ClassMemberRepository classMemberRepository;
    @Mock
    private NotiUtils notiUtils;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ClassNotificationServiceImpl classNotificationService;

    @Test
    void getDetailNotificationInClass_shouldThrowNotPermission_whenStudentNotInClass() {
        // Arrange: student exists but is not a member of the notification's class.
        User student = new User();
        student.setId(6L);
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        ClassNotification noti = ClassNotification.builder().id(10L).clazz(clazz).build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(classNotificationRepository.findById(10L)).thenReturn(Optional.of(noti));
        when(classMemberRepository.existsMemberInClass(1L, 6L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.getDetailNotificationInClass(httpServletRequest, 10L)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldThrowNotPermission_whenTeacherIsNotClassTeacher() {
        // Arrange: caller is teacher but not the owner teacher of the class.
        User teacherCaller = new User();
        teacherCaller.setEmail("teacher1@gmail.com");
        teacherCaller.setRole(ERole.TEACHER);

        User classTeacher = new User();
        classTeacher.setEmail("teacher2@gmail.com");
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .teacher(classTeacher)
                .build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(1L);
        request.setTypeNotification(1);
        request.setDescription("Test noti");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher1@gmail.com");
        when(userRepository.findByEmail("teacher1@gmail.com")).thenReturn(Optional.of(teacherCaller));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.createNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getDetailNotificationInClass_shouldThrowNotExistedUser_whenUserNotFound() {
        // Arrange: token email is resolved but user record does not exist.
        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("missing@gmail.com");
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.getDetailNotificationInClass(httpServletRequest, 10L)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getDetailNotificationInClass_shouldThrowNotExistedNotification_whenNotificationNotFound() {
        // Arrange: user exists but notification id does not exist.
        User student = new User();
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(classNotificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.getDetailNotificationInClass(httpServletRequest, 999L)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.NOTIFICATION, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldThrowNotExistedUser_whenUserNotFound() {
        // Arrange: token resolves but user does not exist in database.
        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(1L);
        request.setTypeNotification(1);
        request.setDescription("Test noti");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("missing@gmail.com");
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.createNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldThrowNotExistedClass_whenClassNotFound() {
        // Arrange: user exists but class id does not exist.
        User teacher = new User();
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(999L);
        request.setTypeNotification(1);
        request.setDescription("Test noti");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.createNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.CLASS, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldHandleZeroClassId() {
        // Arrange: edge case with zero class ID.
        User teacher = new User();
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(0L);  // Edge case: zero ID
        request.setTypeNotification(1);
        request.setDescription("Test noti");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classRepository.findById(0L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.createNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.CLASS, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldHandleLargeClassId() {
        // Arrange: edge case with very large class ID.
        User teacher = new User();
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(Long.MAX_VALUE - 1);  // Boundary: very large ID
        request.setTypeNotification(2);
        request.setDescription("Important update");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classRepository.findById(Long.MAX_VALUE - 1)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.createNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.CLASS, ex.getResponseObject());
    }

    @Test
    void getListNotificationInClass_shouldThrowNotExistedUser_whenUserNotFound() {
        // Arrange: user not found from token.
        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("missing@gmail.com");
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.getListNotificationInClass(httpServletRequest, null, null)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getListNotificationInClass_shouldReturnPage_whenTeacherRequests() {
        // Arrange: teacher can get list without membership restriction.
        User teacher = new User();
        teacher.setId(2L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        User classTeacher = new User();
        classTeacher.setEmail("teacher@gmail.com");
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .teacher(classTeacher)
                .build();
        ClassNotification notification = ClassNotification.builder().id(10L).clazz(clazz).build();

        SearchNotificationInClassDto dto = new SearchNotificationInClassDto();
        dto.setClassId(1L);
        Pageable pageable = PageRequest.of(0, 10);
        ClassNotificationResponse response = ClassNotificationResponse.builder().id(10L).build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classNotificationRepository.findByClazzId(dto, "TEACHER", pageable))
                .thenReturn(new PageImpl<>(List.of(notification), pageable, 1));
        when(convertUtil.convertClassNotificationToDto(httpServletRequest, notification, List.of()))
                .thenReturn(response);
        when(attachDocumentClassRepository.findByClassNotificationId(10L)).thenReturn(List.of());

        // Act
        Page<ClassNotificationResponse> result = classNotificationService.getListNotificationInClass(httpServletRequest, dto, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getId());
    }

    @Test
    void getListNotificationInClass_shouldThrowNotPermission_whenStudentNotInClass() {
        // Arrange: student is blocked if not a member in class.
        User student = new User();
        student.setId(8L);
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        SearchNotificationInClassDto dto = new SearchNotificationInClassDto();
        dto.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classMemberRepository.existsMemberInClass(1L, 8L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.getListNotificationInClass(httpServletRequest, dto, PageRequest.of(0, 10))
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void createNotificationInClass_shouldCreateAndSendNotification_whenTeacherOwnsClass() {
        // Arrange: owner teacher creates class notification successfully.
        User teacher = new User();
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder()
                .id(1L)
                .teacher(teacher)
                .build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassId(1L);
        request.setTypeNotification(1);
        request.setDescription("Weekly update");
        request.setIsPin(true);
        request.setUrlAttachment(List.of("a.pdf"));

        ClassNotification saved = ClassNotification.builder().id(99L).clazz(clazz).createdBy(teacher).build();
        ClassNotificationResponse response = ClassNotificationResponse.builder().id(99L).build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classNotificationRepository.save(any(ClassNotification.class))).thenReturn(saved);
        when(classMemberRepository.findMembersInClass(1L)).thenReturn(List.of(teacher));
        when(attachDocumentClassRepository.findByClassNotificationId(nullable(Long.class))).thenReturn(List.of());
        when(convertUtil.convertClassNotificationToDto(any(HttpServletRequest.class), any(ClassNotification.class), any(List.class)))
                .thenReturn(response);

        // Act
        ClassNotificationResponse result = classNotificationService.createNotificationInClass(httpServletRequest, request);

        // Assert
        assertEquals(99L, result.getId());
        verify(classNotificationRepository).save(any(ClassNotification.class));
        verify(notiUtils).sendNoti(any(List.class), any(), anyString(), anyString(), anyLong());
    }

    @Test
    void updateNotificationInClass_shouldThrowNotPermission_whenCallerNotTeacherOwner() {
        // Arrange: only class teacher can update notification.
        User caller = new User();
        caller.setEmail("other@gmail.com");
        caller.setRole(ERole.TEACHER);

        User owner = new User();
        owner.setEmail("owner@gmail.com");
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().teacher(owner).build();
        ClassNotification notification = ClassNotification.builder().id(15L).clazz(clazz).build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassNotificationId(15L);
        request.setTypeNotification(1);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("other@gmail.com");
        when(userRepository.findByEmail("other@gmail.com")).thenReturn(Optional.of(caller));
        when(classNotificationRepository.findById(15L)).thenReturn(Optional.of(notification));

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.updateNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void updateNotificationInClass_shouldUpdateNotification_whenTeacherOwnerUpdates() {
        // Arrange: owner teacher updates notification and attachments.
        User owner = new User();
        owner.setEmail("owner@gmail.com");
        owner.setRole(ERole.TEACHER);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().teacher(owner).build();
        ClassNotification notification = ClassNotification.builder().id(15L).clazz(clazz).description("old").build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassNotificationId(15L);
        request.setDescription("new");
        request.setTypeNotification(2);
        request.setUrlAttachment(List.of("new-a.pdf", "new-b.pdf"));
        request.setIsPin(true);

        ClassNotificationResponse response = ClassNotificationResponse.builder().id(15L).build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("owner@gmail.com");
        when(userRepository.findByEmail("owner@gmail.com")).thenReturn(Optional.of(owner));
        when(classNotificationRepository.findById(15L)).thenReturn(Optional.of(notification));
        when(attachDocumentClassRepository.findByClassNotificationId(15L)).thenReturn(List.of());
        when(convertUtil.convertClassNotificationToDto(httpServletRequest, notification, List.of())).thenReturn(response);

        // Act
        ClassNotificationResponse result = classNotificationService.updateNotificationInClass(httpServletRequest, request);

        // Assert
        assertEquals(15L, result.getId());
        verify(attachDocumentClassRepository).deleteAllAttachDocumentClassByClassNotificationId(15L);
        verify(attachDocumentClassRepository, org.mockito.Mockito.times(2)).save(any());
    }

    @Test
    void disableOrDeleteNotificationInClass_shouldUpdateFlags_whenTeacherOwnerUpdates() {
        // Arrange: owner teacher toggles active/delete flags.
        User owner = new User();
        owner.setEmail("owner@gmail.com");
        owner.setRole(ERole.TEACHER);

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().teacher(owner).build();
        ClassNotification notification = ClassNotification.builder()
                .id(16L)
                .clazz(clazz)
                .isActive(true)
                .isDelete(false)
                .build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassNotificationId(16L);
        request.setIsActive(false);
        request.setIsDelete(true);

        ClassNotificationResponse response = ClassNotificationResponse.builder().id(16L).build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("owner@gmail.com");
        when(userRepository.findByEmail("owner@gmail.com")).thenReturn(Optional.of(owner));
        when(classNotificationRepository.findById(16L)).thenReturn(Optional.of(notification));
        when(attachDocumentClassRepository.findByClassNotificationId(16L)).thenReturn(List.of());
        when(convertUtil.convertClassNotificationToDto(httpServletRequest, notification, List.of())).thenReturn(response);

        // Act
        ClassNotificationResponse result = classNotificationService.disableOrDeleteNotificationInClass(httpServletRequest, request);

        // Assert
        assertEquals(16L, result.getId());
        assertEquals(false, notification.getIsActive());
        assertEquals(true, notification.getIsDelete());
    }

    @Test
    void updateNotificationInClass_shouldThrowNotExistedNotification_whenNotificationNotFound() {
        // Arrange: notification id does not exist.
        User owner = new User();
        owner.setEmail("owner@gmail.com");
        owner.setRole(ERole.TEACHER);

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassNotificationId(999L);
        request.setTypeNotification(1);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("owner@gmail.com");
        when(userRepository.findByEmail("owner@gmail.com")).thenReturn(Optional.of(owner));
        when(classNotificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.updateNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.NOTIFICATION, ex.getResponseObject());
    }

    @Test
    void disableOrDeleteNotificationInClass_shouldThrowNotPermission_whenCallerIsNotOwner() {
        // Arrange: non-owner teacher cannot disable/delete notification.
        User caller = new User();
        caller.setEmail("caller@gmail.com");
        caller.setRole(ERole.TEACHER);

        User owner = new User();
        owner.setEmail("owner@gmail.com");

        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().teacher(owner).build();
        ClassNotification notification = ClassNotification.builder().id(30L).clazz(clazz).build();

        ClassNotificationRequest request = new ClassNotificationRequest();
        request.setClassNotificationId(30L);
        request.setIsActive(false);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("caller@gmail.com");
        when(userRepository.findByEmail("caller@gmail.com")).thenReturn(Optional.of(caller));
        when(classNotificationRepository.findById(30L)).thenReturn(Optional.of(notification));

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classNotificationService.disableOrDeleteNotificationInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

}
