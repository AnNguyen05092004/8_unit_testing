package com.doan2025.webtoeic.unitTesting.quizAI;

import com.doan2025.webtoeic.constants.enums.EQuizStatus;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Class;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.SearchQuizDto;
import com.doan2025.webtoeic.dto.SearchSubmittedDto;
import com.doan2025.webtoeic.dto.request.QuizRequest;
import com.doan2025.webtoeic.dto.request.SharedQuizRequest;
import com.doan2025.webtoeic.dto.request.SubmitRequest;
import com.doan2025.webtoeic.dto.response.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.impl.QuizServiceImpl;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.doan2025.webtoeic.utils.NotiUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    // ========== Common test data ==========
    private User teacherUser;
    private User studentUser;
    private User managerUser;
    private Quiz quiz;
    private QuizResponse quizResponse;
    private Class clazz;

    @BeforeEach
    void setUp() {
        teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setEmail("teacher@test.com");
        teacherUser.setRole(ERole.TEACHER);
        teacherUser.setFirstName("Nguyen");
        teacherUser.setLastName("Teacher");

        studentUser = new User();
        studentUser.setId(2L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);
        studentUser.setFirstName("Tran");
        studentUser.setLastName("Student");

        managerUser = new User();
        managerUser.setId(3L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);
        managerUser.setFirstName("Le");
        managerUser.setLastName("Manager");

        quiz = Quiz.builder()
                .id(1L)
                .title("TOEIC Practice Test 1")
                .description("Bài test luyện tập TOEIC")
                .totalQuestions(10L)
                .status(EQuizStatus.PRIVATE)
                .createBy(teacherUser)
                .build();

        quizResponse = QuizResponse.builder()
                .id(1L)
                .title("TOEIC Practice Test 1")
                .description("Bài test luyện tập TOEIC")
                .totalQuestions(10L)
                .status(EQuizStatus.PRIVATE)
                .questions(new ArrayList<>())
                .build();

        clazz = new Class();
        clazz.setId(1L);
        clazz.setName("TOEIC Class A");
    }

    // =====================================================================
    // createQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("createQuiz - Tạo quiz")
    class CreateQuizTests {

        @Test
        @DisplayName("TC_QUIZ_001 - Tạo quiz thành công với dữ liệu hợp lệ")
        void createQuiz_WithValidInput_ShouldReturnQuizResponse() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setTitle("New Quiz");
            request.setDescription("Description of new quiz");

            Quiz savedQuiz = Quiz.builder()
                    .id(10L)
                    .title("New Quiz")
                    .description("Description of new quiz")
                    .totalQuestions(0L)
                    .createBy(teacherUser)
                    .build();

            QuizResponse expectedResponse = QuizResponse.builder()
                    .id(10L)
                    .title("New Quiz")
                    .description("Description of new quiz")
                    .totalQuestions(0L)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
            when(convertUtil.convertQuizToDto(savedQuiz)).thenReturn(expectedResponse);

            // Act
            QuizResponse result = quizService.createQuiz(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("New Quiz");
            assertThat(result.getTotalQuestions()).isEqualTo(0L);

            // Verify
            verify(quizRepository).save(any(Quiz.class));
            verify(convertUtil).convertQuizToDto(savedQuiz);
        }

        @Test
        @DisplayName("TC_QUIZ_002 - Tạo quiz thất bại khi user không tồn tại")
        void createQuiz_WithNonExistentUser_ShouldThrowException() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setTitle("New Quiz");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("unknown@test.com");
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.createQuiz(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.USER);
                    });

            verify(quizRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC_QUIZ_003 - Tạo quiz với title trắng không được lưu (kỳ vọng nghiệp vụ)")
        void createQuiz_WithWhiteSpaceTitle_ShouldNotPersistQuiz() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setTitle("     ");
            request.setDescription("Description");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));

            // Act
            quizService.createQuiz(httpServletRequest, request);

            // Assert: title trắng thì không được lưu
            verify(quizRepository, never()).save(any());
        }
    }

    // =====================================================================
    // getQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("getQuiz - Lấy chi tiết quiz")
    class GetQuizTests {

        @Test
        @DisplayName("TC_QUIZ_004 - Lấy quiz thành công theo ID")
        void getQuiz_WithValidId_ShouldReturnQuizResponse() {
            // Arrange
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(convertUtil.convertQuizToDto(quiz)).thenReturn(quizResponse);

            // Act
            QuizResponse result = quizService.getQuiz(httpServletRequest, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("TOEIC Practice Test 1");

            verify(quizRepository).findById(1L);
        }

        @Test
        @DisplayName("TC_QUIZ_005 - Lấy quiz thất bại khi ID không tồn tại")
        void getQuiz_WithNonExistentId_ShouldThrowException() {
            // Arrange
            when(quizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.getQuiz(httpServletRequest, 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUIZ);
                    });
        }
    }

    // =====================================================================
    // getQuizes tests
    // =====================================================================
    @Nested
    @DisplayName("getQuizes - Lấy danh sách quiz")
    class GetQuizesTests {

        @Test
        @DisplayName("TC_QUIZ_006 - Lấy danh sách quiz thành công có phân trang")
        void getQuizes_WithValidParams_ShouldReturnPageOfQuizResponse() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Quiz> quizPage = new PageImpl<>(List.of(quiz), pageable, 1);

            when(quizRepository.filter(dto, pageable)).thenReturn(quizPage);
            when(convertUtil.convertQuizToDto(quiz)).thenReturn(quizResponse);

            // Act
            Page<QuizResponse> result = quizService.getQuizes(httpServletRequest, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("TOEIC Practice Test 1");

            verify(quizRepository).filter(dto, pageable);
        }

        @Test
        @DisplayName("TC_QUIZ_007 - Lấy danh sách quiz rỗng khi không có data")
        void getQuizes_WithNoData_ShouldReturnEmptyPage() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Quiz> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(quizRepository.filter(dto, pageable)).thenReturn(emptyPage);

            // Act
            Page<QuizResponse> result = quizService.getQuizes(httpServletRequest, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // =====================================================================
    // updateQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("updateQuiz - Cập nhật quiz")
    class UpdateQuizTests {

        @Test
        @DisplayName("TC_QUIZ_008 - Cập nhật quiz thành công")
        void updateQuiz_WithValidInput_ShouldReturnUpdatedQuizResponse() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(1L);
            request.setTitle("Updated Title");
            request.setDescription("Updated Description");

            Quiz updatedQuiz = Quiz.builder()
                    .id(1L)
                    .title("Updated Title 36@#!$")
                    .description("Updated Description")
                    .totalQuestions(10L)
                    .createBy(teacherUser)
                    .updateBy(teacherUser)
                    .build();

            QuizResponse expectedResponse = QuizResponse.builder()
                    .id(1L)
                    .title("Updated Title 36@#!$")
                    .description("Updated Description")
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);
            when(convertUtil.convertQuizToDto(any(Quiz.class))).thenReturn(expectedResponse);

            // Act
            QuizResponse result = quizService.updateQuiz(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Updated Title 36@#!$");

            verify(quizRepository).save(any(Quiz.class));
        }

        @Test
        @DisplayName("TC_QUIZ_009 - Cập nhật quiz thất bại khi quiz không tồn tại")
        void updateQuiz_WithNonExistentQuiz_ShouldThrowException() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(999L);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.updateQuiz(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUIZ);
                    });

            verify(quizRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC_QUIZ_010 - Cập nhật quiz với title trắng không được lưu (kỳ vọng nghiệp vụ)")
        void updateQuiz_WithWhiteSpaceTitle_ShouldNotPersistQuiz() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(1L);
            request.setTitle("         "); // Tiêu đề toàn khoảng trắng
            request.setDescription("Updated Description");

            // Chỉ cần Mock đến đoạn kiểm tra dữ liệu, không cần Mock hàm save()
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));

            // Act
            quizService.updateQuiz(httpServletRequest, request);

            // Assert: title trắng thì không được lưu
            verify(quizRepository, never()).save(any());
        }
    }

    // =====================================================================
    // addQuestionToQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("addQuestionToQuiz - Thêm câu hỏi vào quiz")
    class AddQuestionToQuizTests {

        @Test
        @DisplayName("TC_QUIZ_011 - Thêm câu hỏi thành công vào quiz")
        void addQuestionToQuiz_WithValidInput_ShouldReturnUpdatedQuiz() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(1L);
            request.setIdQuestions(List.of(100L, 101L));

            Question question1 = Question.builder().id(100L).content("Question 1").build();
            Question question2 = Question.builder().id(101L).content("Question 2").build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(questionQuizRepository.countByQuizId(1L)).thenReturn(10L);
            when(questionRepository.findById(100L)).thenReturn(Optional.of(question1));
            when(questionRepository.findById(101L)).thenReturn(Optional.of(question2));
            when(questionQuizRepository.save(any(QuestionQuiz.class))).thenReturn(new QuestionQuiz());
            when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
            when(convertUtil.convertQuizToDto(any(Quiz.class))).thenReturn(quizResponse);

            // Act
            QuizResponse result = quizService.addQuestionToQuiz(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();

            // Verify: 2 câu hỏi được lưu vào QuestionQuiz
            verify(questionQuizRepository, times(2)).save(any(QuestionQuiz.class));
            verify(quizRepository).save(any(Quiz.class));
        }

        @Test
        @DisplayName("TC_QUIZ_012 - Thêm câu hỏi thất bại khi question không tồn tại")
        void addQuestionToQuiz_WithNonExistentQuestion_ShouldThrowException() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(1L);
            request.setIdQuestions(List.of(999L));

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(questionQuizRepository.countByQuizId(1L)).thenReturn(10L);
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.addQuestionToQuiz(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUESTION);
                    });
        }
    }

    // =====================================================================
    // removeQuestionFromQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("removeQuestionFromQuiz - Xóa câu hỏi khỏi quiz")
    class RemoveQuestionFromQuizTests {

        @Test
        @DisplayName("TC_QUIZ_013 - Xóa câu hỏi khỏi quiz thành công")
        void removeQuestionFromQuiz_WithValidInput_ShouldReturnUpdatedQuiz() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(1L);
            request.setIdQuestions(List.of(100L, 101L));

            Quiz updatedQuiz = Quiz.builder()
                    .id(1L)
                    .title("TOEIC Practice Test 1")
                    .description("Bài test luyện tập TOEIC")
                    .totalQuestions(8L)
                    .status(EQuizStatus.PRIVATE)
                    .createBy(teacherUser)
                    .updateBy(teacherUser)
                    .build();

            QuizResponse expectedResponse = QuizResponse.builder()
                    .id(1L)
                    .title("TOEIC Practice Test 1")
                    .description("Bài test luyện tập TOEIC")
                    .totalQuestions(8L)
                    .status(EQuizStatus.PRIVATE)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(questionQuizRepository.countByQuizId(1L)).thenReturn(10L);
            when(quizRepository.save(any(Quiz.class))).thenReturn(updatedQuiz);
            when(convertUtil.convertQuizToDto(updatedQuiz)).thenReturn(expectedResponse);

            // Act
            QuizResponse result = quizService.removeQuestionFromQuiz(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTotalQuestions()).isEqualTo(8L);

            // Verify: 2 câu hỏi được xóa khỏi quiz
            verify(questionQuizRepository).deleteQuestionQuizByQuizIdAndQuestionId(1L, 100L);
            verify(questionQuizRepository).deleteQuestionQuizByQuizIdAndQuestionId(1L, 101L);
            verify(quizRepository).save(any(Quiz.class));
        }

        @Test
        @DisplayName("TC_QUIZ_014 - Xóa câu hỏi thất bại khi quiz không tồn tại")
        void removeQuestionFromQuiz_WithNonExistentQuiz_ShouldThrowException() {
            // Arrange
            QuizRequest request = new QuizRequest();
            request.setId(999L);
            request.setIdQuestions(List.of(100L));

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(quizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.removeQuestionFromQuiz(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUIZ);
                    });

            verify(questionQuizRepository, never()).deleteQuestionQuizByQuizIdAndQuestionId(anyLong(), anyLong());
            verify(quizRepository, never()).save(any());
        }

    }

    // =====================================================================
    // convertBankToQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("convertBankToQuiz - Chuyển đổi ngân hàng câu hỏi thành quiz")
    class ConvertBankToQuizTests {

        @Test
        @DisplayName("TC_QUIZ_015 - Chuyển đổi bank thành quiz thành công")
        void convertBankToQuiz_WithValidBank_ShouldReturnQuizResponse() {
            // Arrange
            QuestionBank bank = QuestionBank.builder()
                    .id(1L)
                    .title("Grammar Bank")
                    .build();

            Question q1 = Question.builder().id(1L).content("Q1").build();
            Question q2 = Question.builder().id(2L).content("Q2").build();
            List<Question> questions = List.of(q1, q2);

            Quiz savedQuiz = Quiz.builder()
                    .id(5L)
                    .title("Grammar Bank")
                    .totalQuestions(2L)
                    .status(EQuizStatus.PRIVATE)
                    .createBy(teacherUser)
                    .build();

            QuizResponse expectedResponse = QuizResponse.builder()
                    .id(5L)
                    .title("Grammar Bank")
                    .totalQuestions(2L)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(bank));
            when(questionRepository.findByQuestionBankId(1L)).thenReturn(questions);
            when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
            when(questionQuizRepository.save(any(QuestionQuiz.class))).thenReturn(new QuestionQuiz());
            when(convertUtil.convertQuizToDto(savedQuiz)).thenReturn(expectedResponse);

            // Act
            QuizResponse result = quizService.convertBankToQuiz(httpServletRequest, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Grammar Bank");
            assertThat(result.getTotalQuestions()).isEqualTo(2L);

            // Verify: lưu quiz + 2 questionQuiz
            verify(quizRepository).save(any(Quiz.class));
            verify(questionQuizRepository, times(2)).save(any(QuestionQuiz.class));
        }

        @Test
        @DisplayName("TC_QUIZ_016 - Chuyển đổi bank thất bại khi bank không tồn tại")
        void convertBankToQuiz_WithNonExistentBank_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.convertBankToQuiz(httpServletRequest, 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.BANK);
                    });
        }
    }

    // =====================================================================
    // submitQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("submitQuiz - Nộp bài quiz")
    class SubmitQuizTests {

        @Test
        @DisplayName("TC_QUIZ_017 - Nộp bài quiz thành công")
        void submitQuiz_WithCorrectAnswers_ShouldCalculateScoreCorrectly() {
            // Arrange
            Question question1 = Question.builder().id(1L).content("Q1").build();
            Question question2 = Question.builder().id(2L).content("Q2").build();

            Answer correctAnswer = Answer.builder().id(10L).isCorrect(true).build();
            Answer wrongAnswer = Answer.builder().id(11L).isCorrect(false).build();

            SubmitRequest submitReq1 = new SubmitRequest();
            submitReq1.setQuestionId(1L);
            submitReq1.setAnswerId(10L);
            submitReq1.setStartAt(new Date());
            submitReq1.setEndAt(new Date());

            SubmitRequest submitReq2 = new SubmitRequest();
            submitReq2.setQuestionId(2L);
            submitReq2.setAnswerId(11L);
            submitReq2.setStartAt(new Date());
            submitReq2.setEndAt(new Date());

            List<SubmitRequest> requests = List.of(submitReq1, submitReq2);

            StudentQuiz savedStudentQuiz = StudentQuiz.builder()
                    .id(1L)
                    .user(studentUser)
                    .quiz(quiz)
                    .clazz(clazz)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
            when(studentQuizRepository.save(any(StudentQuiz.class))).thenReturn(savedStudentQuiz);
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question1));
            when(questionRepository.findById(2L)).thenReturn(Optional.of(question2));
            when(answerRepository.findById(10L)).thenReturn(Optional.of(correctAnswer));
            when(answerRepository.findById(11L)).thenReturn(Optional.of(wrongAnswer));
            when(studentAnswerRepository.save(any(StudentAnswer.class))).thenReturn(new StudentAnswer());

            // Act
            quizService.submitQuiz(httpServletRequest, 1L, requests, 1L, "Nộp bài lần 1");

            // Assert & Verify
            // studentQuizRepository.save được gọi 2 lần: 1 lần tạo, 1 lần cập nhật score
            verify(studentQuizRepository, times(2)).save(any(StudentQuiz.class));
            verify(studentAnswerRepository, times(2)).save(any(StudentAnswer.class));
        }

        @Test
        @DisplayName("TC_QUIZ_019 - Nộp bài quiz thất bại khi quiz không tồn tại")
        void submitQuiz_WithNonExistentQuiz_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(quizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.submitQuiz(httpServletRequest, 999L, List.of(), 1L, "test"))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUIZ);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_020 - Nộp bài quiz thất bại khi class không tồn tại")
        void submitQuiz_WithNonExistentClass_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(classRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.submitQuiz(httpServletRequest, 1L, List.of(), 999L, "test"))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.CLASS);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_021 - Nộp bài quiz thành công với tất cả câu đúng, score = 10")
        void submitQuiz_WithAllCorrectAnswers_ShouldGetPerfectScore() {
            // Arrange: quiz có 2 câu, trả lời đúng hết
            Quiz quiz2 = Quiz.builder().id(2L).title("Quiz 2").totalQuestions(2L).build();

            Question q1 = Question.builder().id(1L).build();
            Question q2 = Question.builder().id(2L).build();
            Answer a1 = Answer.builder().id(1L).isCorrect(true).build();
            Answer a2 = Answer.builder().id(2L).isCorrect(true).build();

            SubmitRequest sr1 = new SubmitRequest();
            sr1.setQuestionId(1L);
            sr1.setAnswerId(1L);
            sr1.setStartAt(new Date());
            sr1.setEndAt(new Date());

            SubmitRequest sr2 = new SubmitRequest();
            sr2.setQuestionId(2L);
            sr2.setAnswerId(2L);
            sr2.setStartAt(new Date());
            sr2.setEndAt(new Date());

            StudentQuiz savedSQ = StudentQuiz.builder().id(1L).quiz(quiz2).build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(quizRepository.findById(2L)).thenReturn(Optional.of(quiz2));
            when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
            when(studentQuizRepository.save(any(StudentQuiz.class))).thenReturn(savedSQ);
            when(questionRepository.findById(1L)).thenReturn(Optional.of(q1));
            when(questionRepository.findById(2L)).thenReturn(Optional.of(q2));
            when(answerRepository.findById(1L)).thenReturn(Optional.of(a1));
            when(answerRepository.findById(2L)).thenReturn(Optional.of(a2));
            when(studentAnswerRepository.save(any(StudentAnswer.class))).thenReturn(new StudentAnswer());

            // Act
            quizService.submitQuiz(httpServletRequest, 2L, List.of(sr1, sr2), 1L, "Perfect");

            // Assert: score = 2/2 * 10 = 10.00
            verify(studentQuizRepository, times(2)).save(any(StudentQuiz.class));
        }
    }

    // =====================================================================
    // pullQuizToClass tests
    // =====================================================================
    @Nested
    @DisplayName("pullQuizToClass - Gán quiz vào lớp học")
    class PullQuizToClassTests {

        @Test
        @DisplayName("TC_QUIZ_022 - Gán quiz vào lớp thành công (TEACHER)")
        void pullQuizToClass_AsTeacher_ShouldSaveSharedQuiz() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setQuizId(1L);
            request.setStartAt(new Date());
            request.setEndAt(new Date());

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
            when(shareQuizRepository.save(any(SharedQuiz.class))).thenReturn(new SharedQuiz());
            when(classMemberRepository.findMembersInClass(1L)).thenReturn(List.of(studentUser));
            doNothing().when(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());

            // Act
            quizService.pullQuizToClass(httpServletRequest, request);

            // Assert
            verify(shareQuizRepository).save(any(SharedQuiz.class));
            verify(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), eq(1L));
        }

        @Test
        @DisplayName("TC_QUIZ_023 - Gán quiz thất bại khi teacher không thuộc lớp")
        void pullQuizToClass_AsTeacherNotInClass_ShouldThrowException() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setQuizId(1L);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> quizService.pullQuizToClass(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.USER);
                    });

            verify(shareQuizRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC_QUIZ_024 - Gán quiz thành công (MANAGER - không cần check member)")
        void pullQuizToClass_AsManager_ShouldSaveWithoutMemberCheck() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setQuizId(1L);
            request.setStartAt(new Date());
            request.setEndAt(new Date());

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("manager@test.com");
            when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
            when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
            when(classRepository.findById(1L)).thenReturn(Optional.of(clazz));
            when(shareQuizRepository.save(any(SharedQuiz.class))).thenReturn(new SharedQuiz());
            when(classMemberRepository.findMembersInClass(1L)).thenReturn(List.of(studentUser));
            doNothing().when(notiUtils).sendNoti(anyList(), any(), anyString(), anyString(), anyLong());

            // Act
            quizService.pullQuizToClass(httpServletRequest, request);

            // Assert: không gọi existsMemberInClass vì MANAGER
            verify(classMemberRepository, never()).existsMemberInClass(anyLong(), anyLong());
            verify(shareQuizRepository).save(any(SharedQuiz.class));
        }
    }

    // =====================================================================
    // updateQuizInClass tests
    // =====================================================================
    @Nested
    @DisplayName("updateQuizInClass - Cập nhật quiz trong lớp")
    class UpdateQuizInClassTests {

        @Test
        @DisplayName("TC_QUIZ_025 - Cập nhật quiz trong lớp thành công")
        void updateQuizInClass_WithValidInput_ShouldUpdate() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setSharedQuizId(1L);
            request.setStartAt(new Date());
            request.setEndAt(new Date());
            request.setIsActive(true);

            SharedQuiz sharedQuiz = SharedQuiz.builder()
                    .id(1L)
                    .quiz(quiz)
                    .clazz(clazz)
                    .startAt(new Date())
                    .endAt(new Date())
                    .isActive(true)
                    .isDelete(false)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(shareQuizRepository.findById(1L)).thenReturn(Optional.of(sharedQuiz));
            when(shareQuizRepository.save(any(SharedQuiz.class))).thenReturn(sharedQuiz);

            // Act
            quizService.updateQuizInClass(httpServletRequest, request);

            // Assert
            verify(shareQuizRepository).save(any(SharedQuiz.class));
        }

        @Test
        @DisplayName("TC_QUIZ_026 - Cập nhật thất bại khi teacher không thuộc lớp")
        void updateQuizInClass_TeacherNotInClass_ShouldThrowException() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setSharedQuizId(1L);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> quizService.updateQuizInClass(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_027 - Cập nhật thất bại khi sharedQuiz không tồn tại")
        void updateQuizInClass_SharedQuizNotFound_ShouldThrowException() {
            // Arrange
            SharedQuizRequest request = new SharedQuizRequest();
            request.setClassId(1L);
            request.setSharedQuizId(999L);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(shareQuizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.updateQuizInClass(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUIZ);
                    });
        }
    }

    // =====================================================================
    // getListQuizInClass tests
    // =====================================================================
    @Nested
    @DisplayName("getListQuizInClass - Lấy danh sách quiz trong lớp")
    class GetListQuizInClassTests {

        @Test
        @DisplayName("TC_QUIZ_028 - Lấy danh sách quiz trong lớp thành công (TEACHER)")
        void getListQuizInClass_AsTeacher_ShouldReturnPage() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            Pageable pageable = PageRequest.of(0, 10);

            SharedQuiz sharedQuiz = SharedQuiz.builder()
                    .id(1L).quiz(quiz).clazz(clazz).build();

            Page<SharedQuiz> sharedQuizPage = new PageImpl<>(List.of(sharedQuiz), pageable, 1);

            ShareQuizResponse shareQuizResponse = ShareQuizResponse.builder()
                    .sharedQuizId(1L)
                    .quiz(quizResponse)
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(shareQuizRepository.filter(dto, 1L, pageable)).thenReturn(sharedQuizPage);
            when(convertUtil.convertShareQuizToDto(eq(httpServletRequest), any(SharedQuiz.class)))
                    .thenReturn(shareQuizResponse);

            // Act
            Page<ShareQuizResponse> result = quizService.getListQuizInClass(httpServletRequest, 1L, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSharedQuizId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("TC_QUIZ_029 - Lấy danh sách quiz thất bại khi teacher không thuộc lớp")
        void getListQuizInClass_TeacherNotInClass_ShouldThrowException() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            Pageable pageable = PageRequest.of(0, 10);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> quizService.getListQuizInClass(httpServletRequest, 1L, dto, pageable))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_030 - MANAGER lấy quiz trong lớp không cần check member")
        void getListQuizInClass_AsManager_ShouldSkipMemberCheck() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<SharedQuiz> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("manager@test.com");
            when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
            when(shareQuizRepository.filter(dto, 1L, pageable)).thenReturn(emptyPage);

            // Act
            Page<ShareQuizResponse> result = quizService.getListQuizInClass(httpServletRequest, 1L, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(classMemberRepository, never()).existsMemberInClass(anyLong(), anyLong());
        }
    }

    // =====================================================================
    // getDetailSubmitQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("getDetailSubmitQuiz - Lấy chi tiết bài nộp")
    class GetDetailSubmitQuizTests {

        @Test
        @DisplayName("TC_QUIZ_031 - Lấy chi tiết bài nộp thành công")
        void getDetailSubmitQuiz_WithValidId_ShouldReturnSubmitResponse() {
            // Arrange
            StudentQuiz studentQuiz = StudentQuiz.builder()
                    .id(1L)
                    .user(studentUser)
                    .quiz(quiz)
                    .score(BigDecimal.valueOf(8.5))
                    .startAt(new Date())
                    .endAt(new Date())
                    .des("Bài nộp tốt")
                    .build();

            SubmitResponse expectedResponse = SubmitResponse.builder()
                    .idSubmitted(1L)
                    .score(BigDecimal.valueOf(8.5))
                    .titleQuiz("TOEIC Practice Test 1")
                    .des("Bài nộp tốt")
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(studentQuizRepository.findById(1L)).thenReturn(Optional.of(studentQuiz));
            when(convertUtil.convertSubmitToDto(httpServletRequest, studentQuiz, false))
                    .thenReturn(expectedResponse);

            // Act
            SubmitResponse result = quizService.getDetailSubmitQuiz(httpServletRequest, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getIdSubmitted()).isEqualTo(1L);
            assertThat(result.getScore()).isEqualTo(BigDecimal.valueOf(8.5));
            assertThat(result.getDes()).isEqualTo("Bài nộp tốt");
        }

        @Test
        @DisplayName("TC_QUIZ_032 - Lấy chi tiết bài nộp thất bại khi không tồn tại")
        void getDetailSubmitQuiz_WithNonExistentId_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(studentQuizRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> quizService.getDetailSubmitQuiz(httpServletRequest, 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.SUBMIT);
                    });
        }
    }

    // =====================================================================
    // getListSubmitQuiz tests
    // =====================================================================
    @Nested
    @DisplayName("getListSubmitQuiz - Lấy danh sách bài nộp")
    class GetListSubmitQuizTests {

        @Test
        @DisplayName("TC_QUIZ_033 - Teacher lấy danh sách bài nộp thành công")
        void getListSubmitQuiz_AsTeacher_ShouldReturnAllSubmissions() {
            // Arrange
            SearchSubmittedDto dto = new SearchSubmittedDto();
            Pageable pageable = PageRequest.of(0, 10);

            StudentQuiz sq = StudentQuiz.builder()
                    .id(1L).user(studentUser).quiz(quiz).clazz(clazz)
                    .score(BigDecimal.valueOf(7.0)).build();

            Page<StudentQuiz> sqPage = new PageImpl<>(List.of(sq), pageable, 1);

            SubmitResponse submitResponse = SubmitResponse.builder()
                    .idSubmitted(1L).score(BigDecimal.valueOf(7.0)).build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(studentQuizRepository.filter(1L, 1L, dto, pageable, null)).thenReturn(sqPage);
            when(convertUtil.convertSubmitToDto(eq(httpServletRequest), any(StudentQuiz.class), eq(true)))
                    .thenReturn(submitResponse);

            // Act
            Page<SubmitResponse> result = quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            // Teacher xem tất cả (email = null)
            verify(studentQuizRepository).filter(1L, 1L, dto, pageable, null);
        }

        @Test
        @DisplayName("TC_QUIZ_034 - Student chỉ xem bài nộp của mình")
        void getListSubmitQuiz_AsStudent_ShouldFilterByEmail() {
            // Arrange
            SearchSubmittedDto dto = new SearchSubmittedDto();
            Pageable pageable = PageRequest.of(0, 10);

            Page<StudentQuiz> sqPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("student@test.com");
            when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
            when(classMemberRepository.existsMemberInClass(1L, 2L)).thenReturn(true);
            when(studentQuizRepository.filter(1L, 1L, dto, pageable, "student@test.com")).thenReturn(sqPage);

            // Act
            Page<SubmitResponse> result = quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, dto, pageable);

            // Assert
            assertThat(result).isNotNull();

            // Student filter bằng email riêng
            verify(studentQuizRepository).filter(1L, 1L, dto, pageable, "student@test.com");
        }

        @Test
        @DisplayName("TC_QUIZ_035 - Teacher/Student không thuộc lớp → lỗi permission")
        void getListSubmitQuiz_NotInClass_ShouldThrowException() {
            // Arrange
            SearchSubmittedDto dto = new SearchSubmittedDto();
            Pageable pageable = PageRequest.of(0, 10);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, dto, pageable))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_036 - Manager lấy danh sách bài nộp không cần check member")
        void getListSubmitQuiz_AsManager_ShouldSkipMemberCheck() {
            // Arrange
            SearchSubmittedDto dto = new SearchSubmittedDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<StudentQuiz> sqPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("manager@test.com");
            when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
            when(studentQuizRepository.filter(1L, 1L, dto, pageable, null)).thenReturn(sqPage);

            // Act
            Page<SubmitResponse> result = quizService.getListSubmitQuiz(httpServletRequest, 1L, 1L, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            verify(classMemberRepository, never()).existsMemberInClass(anyLong(), anyLong());
        }
    }

    // =====================================================================
    // statisticDetailQuizInClass tests
    // =====================================================================
    @Nested
    @DisplayName("statisticDetailQuizInClass - Thống kê chi tiết quiz trong lớp")
    class StatisticDetailQuizInClassTests {

        @Test
        @DisplayName("TC_QUIZ_037 - Thống kê chi tiết quiz thành công")
        void statisticDetailQuizInClass_WithValidData_ShouldReturnOverview() {
            // Arrange
            SearchSubmittedDto dto = new SearchSubmittedDto();
            Page<StudentQuiz> totalPage = new PageImpl<>(
                    List.of(new StudentQuiz(), new StudentQuiz(), new StudentQuiz(), new StudentQuiz()),
                    PageRequest.of(0, 10), 4);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(studentQuizRepository.filter(1L, 1L, dto, null, null)).thenReturn(totalPage);
            when(studentQuizRepository.countOver(1L, dto, 5L)).thenReturn(3L);

            // Act
            OverviewResponse result = quizService.statisticDetailQuizInClass(
                    httpServletRequest, 1L, 1L, 5L, dto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(4L);
            assertThat(result.getOverScore()).isEqualTo(3L);
            assertThat(result.getUnderScore()).isEqualTo(1L);
        }
    }

    // =====================================================================
    // statisticOverviewQuizInClass tests
    // =====================================================================
    @Nested
    @DisplayName("statisticOverviewQuizInClass - Thống kê tổng quan quiz trong lớp")
    class StatisticOverviewQuizInClassTests {

        @Test
        @DisplayName("TC_QUIZ_038 - Thống kê tổng quan quiz thành công")
        void statisticOverviewQuizInClass_WithValidData_ShouldReturnOverview() {
            // Arrange
            SearchQuizDto dto = new SearchQuizDto();
            SharedQuiz sq1 = SharedQuiz.builder().id(1L).build();
            SharedQuiz sq2 = SharedQuiz.builder().id(2L).build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(shareQuizRepository.filter(dto, 1L)).thenReturn(List.of(sq1, sq2));
            when(shareQuizRepository.statisticOverviewOverScoreQuizInClass(1L, dto, 5L)).thenReturn(1L);

            // Act
            OverviewResponse result = quizService.statisticOverviewQuizInClass(
                    httpServletRequest, 1L, 5L, dto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotal()).isEqualTo(2L);
            assertThat(result.getOverScore()).isEqualTo(1L);
            assertThat(result.getUnderScore()).isEqualTo(1L);
        }
    }

    // =====================================================================
    // overviewStudentSubmitInClass tests
    // =====================================================================
    @Nested
    @DisplayName("overviewStudentSubmitInClass - Tổng quan bài nộp sinh viên")
    class OverviewStudentSubmitInClassTests {

        @Test
        @DisplayName("TC_QUIZ_039 - Teacher lấy tổng quan bài nộp thành công")
        void overviewStudentSubmitInClass_AsTeacher_ShouldReturnOverview() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            ClassMember classMember = ClassMember.builder()
                    .id(1L).member(studentUser).clazz(clazz).build();

            SharedQuiz sharedQuiz = SharedQuiz.builder()
                    .id(1L).quiz(quiz).clazz(clazz).build();

            StudentQuiz studentQuiz = StudentQuiz.builder()
                    .id(1L)
                    .quiz(quiz)
                    .user(studentUser)
                    .score(BigDecimal.valueOf(8.0))
                    .startAt(new Date())
                    .endAt(new Date())
                    .des("Good")
                    .build();

            UserResponse userResponse = new UserResponse();
            userResponse.setId(2L);
            userResponse.setFirstName("Tran");
            userResponse.setLastName("Student");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(classMemberRepository.findMembersInClass(any(com.doan2025.webtoeic.dto.SearchMemberInClassDto.class)))
                    .thenReturn(List.of(classMember));
            when(shareQuizRepository.filter(any(SearchQuizDto.class), eq(1L))).thenReturn(List.of(sharedQuiz));
            when(studentQuizRepository.findByUser_idAndClazz_id(2L, 1L)).thenReturn(List.of(studentQuiz));
            when(modelMapper.map(studentUser, UserResponse.class)).thenReturn(userResponse);

            // Act
            Page<OverviewStudentSubmit> result = quizService.overviewStudentSubmitInClass(
                    httpServletRequest, 1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            OverviewStudentSubmit overview = result.getContent().get(0);
            assertThat(overview.getUserResponse().getFirstName()).isEqualTo("Tran");
            assertThat(overview.getQuizSubmit()).hasSize(1);
            assertThat(overview.getQuizSubmit().get(0).getScore()).isEqualTo(BigDecimal.valueOf(8.0));
        }

        @Test
        @DisplayName("TC_QUIZ_040 - Teacher không thuộc lớp → lỗi permission")
        void overviewStudentSubmitInClass_TeacherNotInClass_ShouldThrowException() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> quizService.overviewStudentSubmitInClass(
                    httpServletRequest, 1L, pageable))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_PERMISSION);
                    });
        }

        @Test
        @DisplayName("TC_QUIZ_041 - Sinh viên chưa làm bài → hiển thị 'Chưa làm'")
        void overviewStudentSubmitInClass_StudentNotSubmitted_ShouldShowNotDone() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            ClassMember classMember = ClassMember.builder()
                    .id(1L).member(studentUser).clazz(clazz).build();

            SharedQuiz sharedQuiz = SharedQuiz.builder()
                    .id(1L).quiz(quiz).clazz(clazz).build();

            UserResponse userResponse = new UserResponse();
            userResponse.setId(2L);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(classMemberRepository.existsMemberInClass(1L, 1L)).thenReturn(true);
            when(classMemberRepository.findMembersInClass(any(com.doan2025.webtoeic.dto.SearchMemberInClassDto.class)))
                    .thenReturn(List.of(classMember));
            when(shareQuizRepository.filter(any(SearchQuizDto.class), eq(1L))).thenReturn(List.of(sharedQuiz));
            when(studentQuizRepository.findByUser_idAndClazz_id(2L, 1L)).thenReturn(Collections.emptyList());
            when(modelMapper.map(studentUser, UserResponse.class)).thenReturn(userResponse);

            // Act
            Page<OverviewStudentSubmit> result = quizService.overviewStudentSubmitInClass(
                    httpServletRequest, 1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            OverviewStudentSubmit overview = result.getContent().get(0);
            assertThat(overview.getQuizSubmit()).hasSize(1);
            assertThat(overview.getQuizSubmit().get(0).getScore()).isNull();
            assertThat(overview.getQuizSubmit().get(0).getDes()).isEqualTo("Chưa làm");
        }

        @Test
        @DisplayName("TC_QUIZ_042 - MANAGER lấy tổng quan không cần kiểm tra member")
        void overviewStudentSubmitInClass_AsManager_ShouldWork() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("manager@test.com");
            when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
            when(classMemberRepository.findMembersInClass(any(com.doan2025.webtoeic.dto.SearchMemberInClassDto.class)))
                    .thenReturn(Collections.emptyList());
            when(shareQuizRepository.filter(any(SearchQuizDto.class), eq(1L))).thenReturn(Collections.emptyList());

            // Act
            Page<OverviewStudentSubmit> result = quizService.overviewStudentSubmitInClass(
                    httpServletRequest, 1L, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }
    }
}
