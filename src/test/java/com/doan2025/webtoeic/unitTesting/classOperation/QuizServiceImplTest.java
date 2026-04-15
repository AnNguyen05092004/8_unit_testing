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
import static org.mockito.Mockito.times;
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

    @Test
    void pullQuizToClass_shouldCreateSharedQuiz_whenCallerIsConsultant() {
        // Arrange: consultant can pull a quiz to any class
        com.doan2025.webtoeic.domain.User consultant = new com.doan2025.webtoeic.domain.User();
        consultant.setId(5L);
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        SharedQuizRequest request = new SharedQuizRequest();
        request.setQuizId(1L);
        request.setClassId(1L);
        request.setStartAt(new Date());
        request.setEndAt(new Date());

        com.doan2025.webtoeic.domain.Quiz quiz = new com.doan2025.webtoeic.domain.Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");

        com.doan2025.webtoeic.domain.Class clazz = new com.doan2025.webtoeic.domain.Class();
        clazz.setId(1L);
        clazz.setName("Class 1");

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(classMemberRepository.findMembersInClass(1L)).thenReturn(List.of(consultant));

        // Act
        quizService.pullQuizToClass(httpServletRequest, request);

        // Assert
        verify(shareQuizRepository).save(any());
        verify(notiUtils).sendNoti(any(), any(), any(), any(), any());
    }

    @Test
    void pullQuizToClass_shouldThrowNotPermission_whenTeacherNotInClass() {
        // Arrange: teacher must be member of class to pull quiz
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        SharedQuizRequest request = new SharedQuizRequest();
        request.setQuizId(1L);
        request.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(false);

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.pullQuizToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        assertEquals(ResponseObject.USER, ex.getResponseObject());
    }

    @Test
    void pullQuizToClass_shouldThrowNotExisted_whenQuizNotFound() {
        // Arrange: quiz does not exist
        com.doan2025.webtoeic.domain.User manager = new com.doan2025.webtoeic.domain.User();
        manager.setId(4L);
        manager.setEmail("manager@gmail.com");
        manager.setRole(ERole.MANAGER);

        SharedQuizRequest request = new SharedQuizRequest();
        request.setQuizId(999L);
        request.setClassId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("manager@gmail.com");
        when(userRepository.findByEmail("manager@gmail.com")).thenReturn(Optional.of(manager));
        when(quizRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        WebToeicException ex = assertThrows(
                WebToeicException.class,
                () -> quizService.pullQuizToClass(httpServletRequest, request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        assertEquals(ResponseObject.QUIZ, ex.getResponseObject());
    }

    @Test
    void submitQuiz_shouldCalculateScoreCorrectly_whenStudentSubmitsAnswers() {
        // Arrange: student submits quiz with mix of correct and incorrect answers
        com.doan2025.webtoeic.domain.User student = new com.doan2025.webtoeic.domain.User();
        student.setId(2L);
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        com.doan2025.webtoeic.domain.Quiz quiz = new com.doan2025.webtoeic.domain.Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");
        quiz.setTotalQuestions(10L);

        com.doan2025.webtoeic.domain.Class clazz = new com.doan2025.webtoeic.domain.Class();
        clazz.setId(1L);
        clazz.setName("Class 1");

        com.doan2025.webtoeic.domain.Question q1 = new com.doan2025.webtoeic.domain.Question();
        q1.setId(1L);

        com.doan2025.webtoeic.domain.Answer a1 = new com.doan2025.webtoeic.domain.Answer();
        a1.setId(1L);
        a1.setIsCorrect(true);

        com.doan2025.webtoeic.dto.request.SubmitRequest submitReq = new com.doan2025.webtoeic.dto.request.SubmitRequest();
        submitReq.setQuestionId(1L);
        submitReq.setAnswerId(1L);
        submitReq.setStartAt(new Date());
        submitReq.setEndAt(new Date());

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(answerRepository.findById(1L)).thenReturn(Optional.of(a1));
        when(studentQuizRepository.save(any())).thenAnswer(invocation -> {
            com.doan2025.webtoeic.domain.StudentQuiz sq = invocation.getArgument(0);
            sq.setId(1L);
            return sq;
        });

        // Act
        quizService.submitQuiz(httpServletRequest, 1L, List.of(submitReq), 1L, "Test submission");

        // Assert
        verify(studentQuizRepository, times(2)).save(any()); // save twice: create + update score
        verify(studentAnswerRepository).save(any());
        verify(questionRepository).findById(1L);
        verify(answerRepository).findById(1L);
    }

    @Test
    void submitQuiz_shouldHandleEmptyRequests_whenNoAnswersSubmitted() {
        // Arrange: student submits empty quiz
        com.doan2025.webtoeic.domain.User student = new com.doan2025.webtoeic.domain.User();
        student.setId(2L);
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        com.doan2025.webtoeic.domain.Quiz quiz = new com.doan2025.webtoeic.domain.Quiz();
        quiz.setId(1L);
        quiz.setTitle("Test Quiz");
        quiz.setTotalQuestions(10L);

        com.doan2025.webtoeic.domain.Class clazz = new com.doan2025.webtoeic.domain.Class();
        clazz.setId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
        when(studentQuizRepository.save(any())).thenAnswer(invocation -> {
            com.doan2025.webtoeic.domain.StudentQuiz sq = invocation.getArgument(0);
            sq.setId(1L);
            return sq;
        });

        // Act
        quizService.submitQuiz(httpServletRequest, 1L, List.of(), 1L, "No answers");

        // Assert
        verify(studentQuizRepository, times(2)).save(any()); // save twice: create + update score
    }

    @Test
    void getListSubmitQuiz_shouldReturnOnlyStudentSubmissions_whenCallerIsStudent() {
        // Arrange: student should see only their own submissions
        com.doan2025.webtoeic.domain.User student = new com.doan2025.webtoeic.domain.User();
        student.setId(2L);
        student.setEmail("student@gmail.com");
        student.setRole(ERole.STUDENT);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@gmail.com");
        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));
        when(classMemberRepository.existsMemberInClass(1L, 2L)).thenReturn(true);
        when(studentQuizRepository.filter(eq(1L), eq(1L), any(), any(), eq("student@gmail.com")))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        // Act
        quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, new com.doan2025.webtoeic.dto.SearchSubmittedDto(), PageRequest.of(0, 10));

        // Assert
        verify(studentQuizRepository).filter(eq(1L), eq(1L), any(), any(), eq("student@gmail.com"));
    }

    @Test
    void getListSubmitQuiz_shouldReturnAllSubmissions_whenCallerIsTeacher() {
        // Arrange: teacher should see all submissions in class
        com.doan2025.webtoeic.domain.User teacher = new com.doan2025.webtoeic.domain.User();
        teacher.setId(3L);
        teacher.setEmail("teacher@gmail.com");
        teacher.setRole(ERole.TEACHER);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@gmail.com");
        when(userRepository.findByEmail("teacher@gmail.com")).thenReturn(Optional.of(teacher));
        when(classMemberRepository.existsMemberInClass(1L, 3L)).thenReturn(true);
        when(studentQuizRepository.filter(eq(1L), eq(1L), any(), any(), eq(null)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));

        // Act
        quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, new com.doan2025.webtoeic.dto.SearchSubmittedDto(), PageRequest.of(0, 10));

        // Assert
        verify(studentQuizRepository).filter(eq(1L), eq(1L), any(), any(), eq(null));
    }

    @Test
    void statisticOverviewQuizInClass_shouldReturnStatistics_whenDataExists() {
        // Arrange: calculate statistics for quizzes in class
        com.doan2025.webtoeic.domain.User consultant = new com.doan2025.webtoeic.domain.User();
        consultant.setId(5L);
        consultant.setEmail("consultant@gmail.com");
        consultant.setRole(ERole.CONSULTANT);

        com.doan2025.webtoeic.domain.SharedQuiz quiz1 = new com.doan2025.webtoeic.domain.SharedQuiz();
        quiz1.setId(1L);

        when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("consultant@gmail.com");
        when(userRepository.findByEmail("consultant@gmail.com")).thenReturn(Optional.of(consultant));
        when(shareQuizRepository.filter(any(), eq(1L))).thenReturn(List.of(quiz1));
        when(shareQuizRepository.statisticOverviewOverScoreQuizInClass(eq(1L), any(), eq(5L))).thenReturn(1L);

        // Act
        com.doan2025.webtoeic.dto.response.OverviewResponse result = quizService.statisticOverviewQuizInClass(
                httpServletRequest, 1L, 5L, new SearchQuizDto()
        );

        // Assert
        assertEquals(1L, result.getTotal());
        assertEquals(1L, result.getOverScore());
        assertEquals(0L, result.getUnderScore());
    }

}
