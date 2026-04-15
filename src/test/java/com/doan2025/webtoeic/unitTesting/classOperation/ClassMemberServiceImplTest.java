package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.EJoinStatus;
import com.doan2025.webtoeic.constants.enums.ENotiType;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.ClassMember;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchMemberInClassDto;
import com.doan2025.webtoeic.dto.request.ClassRequest;
import com.doan2025.webtoeic.dto.response.ClassMemberResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassMemberServiceImplTest {

    @Mock
    private ClassRepository classRepository;
    @Mock
    private ClassMemberRepository classMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private NotiUtils notiUtils;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ClassMemberServiceImpl classMemberService;

    @Test
    void addUserToClass_shouldSaveMemberAndSendNoti_whenMemberNotExists() {
        // Arrange: class exists and target user is not yet a class member.
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        User student = new User();
        student.setId(6L);
        student.setRole(ERole.STUDENT);

        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(6L))
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(userRepository.findById(6L)).thenReturn(Optional.of(student));
        when(classMemberRepository.findByClassAndMember(6L, 1L)).thenReturn(null);

        // Act
        classMemberService.addUserToClass(httpServletRequest, request);

        // Assert: member record is saved and notification is dispatched.
        verify(classMemberRepository).save(any(ClassMember.class));
        verify(notiUtils).sendNoti(anyList(), any(ENotiType.class), any(String.class), any(String.class), any(Long.class));
    }

    @Test
    void removeUserFromClass_shouldThrowNotExisted_whenNoMembersFound() {
        // Arrange: no class members found for the remove request.
        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(6L))
                .build();

        when(classMemberRepository.findByClassAndUser(request)).thenReturn(Collections.emptyList());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classMemberService.removeUserFromClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void addUserToClass_shouldReactivate_whenMemberExistsWithDroppedStatus() {
        // Arrange: existing member is dropped and should be re-activated.
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        User student = new User();
        student.setId(6L);
        student.setRole(ERole.STUDENT);

        ClassMember existing = ClassMember.builder().status(EJoinStatus.DROPPED).build();

        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(6L))
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(userRepository.findById(6L)).thenReturn(Optional.of(student));
        when(classMemberRepository.findByClassAndMember(6L, 1L)).thenReturn(existing);

        // Act
        classMemberService.addUserToClass(httpServletRequest, request);

        // Assert
        assertEquals(EJoinStatus.ACTIVE, existing.getStatus());
        verify(classMemberRepository).save(existing);
    }

    @Test
    void addUserToClass_shouldThrowNotExistedClass_whenClassNotFound() {
        // Arrange: class id does not exist.
        ClassRequest request = ClassRequest.builder()
                .id(99L)
                .memberIds(List.of(6L))
                .build();

        when(classRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classMemberService.addUserToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.CLASS, ex.getResponseObject());
    }

    @Test
    void addUserToClass_shouldThrowNotExistedUser_whenMemberIdNotFound() {
        // Arrange: class exists but target member id does not exist.
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(66L))
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(userRepository.findById(66L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classMemberService.addUserToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void addUserToClass_shouldHandleMultipleMembers_whenAllValid() {
        // Arrange: add multiple different members in one request.
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        User student1 = new User();
        student1.setId(6L);
        student1.setRole(ERole.STUDENT);
        User student2 = new User();
        student2.setId(7L);
        student2.setRole(ERole.STUDENT);

        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(6L, 7L))
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(userRepository.findById(6L)).thenReturn(Optional.of(student1));
        when(userRepository.findById(7L)).thenReturn(Optional.of(student2));
        when(classMemberRepository.findByClassAndMember(6L, 1L)).thenReturn(null);
        when(classMemberRepository.findByClassAndMember(7L, 1L)).thenReturn(null);

        // Act
        classMemberService.addUserToClass(httpServletRequest, request);

        // Assert: both members are saved
        verify(classMemberRepository, org.mockito.Mockito.times(2)).save(any(ClassMember.class));
    }

    @Test
    void addUserToClass_shouldHandleEmptyMemberIdList_withoutError() {
        // Arrange: empty member id list (edge case).
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(Collections.emptyList())
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));

        // Act (no members to add)
        classMemberService.addUserToClass(httpServletRequest, request);

        // Assert: loop skipped, no save/noti calls
        verify(classMemberRepository, org.mockito.Mockito.never()).save(any(ClassMember.class));
        verify(notiUtils, org.mockito.Mockito.never()).sendNoti(anyList(), any(), any(), any(), any());
    }

    @Test
    void removeUserFromClass_shouldSetStatusDroppedForMultipleMembers() {
        // Arrange: remove multiple members at once.
        ClassMember member1 = ClassMember.builder().id(1L).status(EJoinStatus.ACTIVE).build();
        ClassMember member2 = ClassMember.builder().id(2L).status(EJoinStatus.ACTIVE).build();
        List<ClassMember> memberList = List.of(member1, member2);

        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(6L, 7L))
                .build();

        when(classMemberRepository.findByClassAndUser(request)).thenReturn(memberList);

        // Act
        classMemberService.removeUserFromClass(httpServletRequest, request);

        // Assert: both members set to DROPPED
        verify(classMemberRepository, org.mockito.Mockito.times(2)).save(any(ClassMember.class));
        assertEquals(EJoinStatus.DROPPED, member1.getStatus());
        assertEquals(EJoinStatus.DROPPED, member2.getStatus());
    }

    @Test
    void addUserToClass_shouldThrowNotExistedClass_whenClassIdIsZero() {
        // Arrange: class id is 0 (boundary edge case).
        ClassRequest request = ClassRequest.builder()
                .id(0L)
                .memberIds(List.of(6L))
                .build();

        when(classRepository.findById(0L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classMemberService.addUserToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
    }

    @Test
    void addUserToClass_shouldThrowNotExistedUser_whenMemberIdIsNegative() {
        // Arrange: negative member id (data validation).
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(List.of(-1L))
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(userRepository.findById(-1L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> classMemberService.addUserToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
    }

    @Test
    void addUserToClass_shouldThrowException_whenMemberIdsListIsNull() {
        // Arrange: member IDs list is null (invalid data type).
        com.doan2025.webtoeic.domain.Class clazz = com.doan2025.webtoeic.domain.Class.builder().id(1L).build();
        ClassRequest request = ClassRequest.builder()
                .id(1L)
                .memberIds(null)  // Invalid: null list
                .build();

        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));

        // Act + Assert: should throw exception when accessing null list
        assertThrows(
                Exception.class,
                () -> classMemberService.addUserToClass(httpServletRequest, request)
        );
    }

    @Test
    void getMemberInClass_shouldFilterByCallerEmail_whenCallerIsStudent() {
        // Arrange: student caller should query with their own email.
        User student = new User();
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        SearchMemberInClassDto request = SearchMemberInClassDto.builder()
                .classId(1L)
                .searchString("math")
                .status(List.of("ACTIVE"))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        ClassMember classMember = ClassMember.builder()
                .member(student)
                .roleInClass(ERole.STUDENT)
                .status(EJoinStatus.ACTIVE)
                .build();
        ClassMemberResponse response = ClassMemberResponse.builder().email("student@gmail.com").build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(classMemberRepository.findMembersInClass(request, "student@gmail.com", pageable))
                .thenReturn(new PageImpl<>(List.of(classMember), pageable, 1));
        when(convertUtil.convertClassMemberToDto(httpServletRequest, classMember)).thenReturn(response);

        // Act
        Page<ClassMemberResponse> result = classMemberService.getMemberInClass(httpServletRequest, request, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("student@gmail.com", result.getContent().get(0).getEmail());
    }

    @Test
    void getMemberInClass_shouldQueryWithoutEmail_whenCallerIsTeacher() {
        // Arrange: teacher caller should query without restricted email filter.
        User teacher = new User();
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SearchMemberInClassDto request = SearchMemberInClassDto.builder()
                .classId(1L)
                .searchString(null)
                .status(Collections.emptyList())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        ClassMember classMember = ClassMember.builder()
                .member(teacher)
                .roleInClass(ERole.TEACHER)
                .status(EJoinStatus.ACTIVE)
                .build();
        ClassMemberResponse response = ClassMemberResponse.builder().email("teacher@gmail.com").build();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.findMembersInClass(request, null, pageable))
                .thenReturn(new PageImpl<>(List.of(classMember), pageable, 1));
        when(convertUtil.convertClassMemberToDto(httpServletRequest, classMember)).thenReturn(response);

        // Act
        Page<ClassMemberResponse> result = classMemberService.getMemberInClass(httpServletRequest, request, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("teacher@gmail.com", result.getContent().get(0).getEmail());
    }

}
