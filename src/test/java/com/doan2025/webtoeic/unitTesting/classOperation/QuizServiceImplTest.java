package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.SharedQuiz;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import com.doan2025.webtoeic.dto.request.SharedQuizRequest;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.AnswerRepository;
import com.doan2025.webtoeic.repository.ClassMemberRepository;
import com.doan2025.webtoeic.repository.ClassRepository;
import com.doan2025.webtoeic.repository.QuestionBankRepository;
import com.doan2025.webtoeic.repository.QuestionQuizRepository;
import com.doan2025.webtoeic.repository.QuestionRepository;
import com.doan2025.webtoeic.repository.QuizRepository;
import com.doan2025.webtoeic.repository.ShareQuizRepository;
import com.doan2025.webtoeic.repository.StudentAnswerRepository;
import com.doan2025.webtoeic.repository.StudentQuizRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionBankRepository questionBankRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuestionQuizRepository questionQuizRepository;
    @Mock
    private ClassRepository classRepository;
    @Mock
    private ShareQuizRepository shareQuizRepository;
    @Mock
    private StudentQuizRepository studentQuizRepository;
    @Mock
    private StudentAnswerRepository studentAnswerRepository;
    @Mock
    private ClassMemberRepository classMemberRepository;
    @Mock
    private NotiUtils notiUtils;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Test
    void getListQuizInClass_shouldThrowNotPermission_whenTeacherIsNotMember() {
        // Arrange: caller is teacher but not a class member.
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SearchQuizDto dto = new SearchQuizDto();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.getListQuizInClass(httpServletRequest, 1L, dto, PageRequest.of(0, 10))
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getListQuizInClass_shouldThrowNotExistedUser_whenUserNotFound() {
        // Arrange: token email does not map to a user.
        SearchQuizDto dto = new SearchQuizDto();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("missing@gmail.com");
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.getListQuizInClass(httpServletRequest, 1L, dto, PageRequest.of(0, 10))
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getListQuizInClass_shouldThrowNotPermission_whenZeroClassId() {
        // Arrange: edge case with zero class ID - should not find any members.
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SearchQuizDto dto = new SearchQuizDto();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(0L, 3L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.getListQuizInClass(httpServletRequest, 0L, dto, PageRequest.of(0, 10))
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void getListQuizInClass_shouldThrowException_whenPageRequestIsNull() {
        // Arrange: null page request (invalid data type).
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SearchQuizDto searchDto = new SearchQuizDto();

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(true);

        assertThrows(
                Exception.class,
                () -> quizService.getListQuizInClass(httpServletRequest, 1L, searchDto, null)
        );
    }

    @Test
    void overviewStudentSubmitInClass_shouldReturnPage_whenCallerIsConsultant() {
        // Arrange: consultant is allowed to view overview statistics.
        com.doan2025.webtoeic.domain.User consultant = new com.doan2025.webtoeic.domain.User();
        consultant.setId(10L);
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        PageRequest pageable = PageRequest.of(0, 10);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(classMemberRepository.findMembersInClass(org.mockito.ArgumentMatchers.any(com.doan2025.webtoeic.dto.SearchMemberInClassDto.class))).thenReturn(List.of());
        when(shareQuizRepository.filter(any(SearchQuizDto.class), eq(1L))).thenReturn(List.of());

        // Act
        Page<?> result = quizService.overviewStudentSubmitInClass(httpServletRequest, 1L, pageable);

        // Assert
        assertEquals(0, result.getContent().size());
    }

    @Test
    void overviewStudentSubmitInClass_shouldThrowNotPermission_whenTeacherNotInClass() {
        // Arrange: teacher must be class member to view overview.
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.overviewStudentSubmitInClass(httpServletRequest, 1L, PageRequest.of(0, 10))
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void updateQuizInClass_shouldUpdateAndSave_whenCallerIsConsultant() {
        // Arrange: consultant can update shared quiz without class-member check.
        com.doan2025.webtoeic.domain.User consultant = new com.doan2025.webtoeic.domain.User();
        consultant.setId(10L);
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        SharedQuiz sharedQuiz = SharedQuiz.builder()
                .id(20L)
                .startAt(new Date(System.currentTimeMillis() + 60_000))
                .endAt(new Date(System.currentTimeMillis() + 120_000))
                .isActive(true)
                .isDelete(false)
                .build();

        SharedQuizRequest request = new SharedQuizRequest();
        request.setSharedQuizId(20L);
        request.setClassId(1L);
        request.setStartAt(new Date(System.currentTimeMillis() + 180_000));
        request.setEndAt(new Date(System.currentTimeMillis() + 240_000));
        request.setIsActive(false);
        request.setIsDelete(true);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(shareQuizRepository.findById(20L)).thenReturn(Optional.of(sharedQuiz));

        // Act
        quizService.updateQuizInClass(httpServletRequest, request);

        // Assert
        verify(shareQuizRepository).save(sharedQuiz);
        assertEquals(false, sharedQuiz.getIsActive());
        assertEquals(true, sharedQuiz.getIsDelete());
        assertEquals(consultant, sharedQuiz.getUpdatedBy());
    }

    @Test
    void updateQuizInClass_shouldThrowNotPermission_whenTeacherNotInClass() {
        // Arrange: teacher cannot update shared quiz if not a member of target class.
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SharedQuizRequest request = new SharedQuizRequest();
        request.setSharedQuizId(20L);
        request.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.updateQuizInClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

}
