package com.doan2025.webtoeic.unitTesting.quizAI;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.*;
import com.doan2025.webtoeic.dto.SearchQuestionDto;
import com.doan2025.webtoeic.dto.request.AnswerRequest;
import com.doan2025.webtoeic.dto.request.ExplanationQuestionRequest;
import com.doan2025.webtoeic.dto.request.QuestionRequest;
import com.doan2025.webtoeic.dto.response.*;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.*;
import com.doan2025.webtoeic.service.AnswerService;
import com.doan2025.webtoeic.service.ExplanationQuestionService;
import com.doan2025.webtoeic.service.impl.QuestionServiceImpl;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestionServiceImpl.
 * Scope: UT_QST_001 – UT_QST_014 (unit_test_plan.md §3.2)
 * Mapped system tests: TC_FN_020–031, TC_FN_038–041
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

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
    private ExplanationQuestionRepository explanationQuestionRepository;
    @Mock
    private QuestionBankRepository questionBankRepository;
    @Mock
    private RangeTopicRepository rangeTopicRepository;
    @Mock
    private ScoreScaleRepository scoreScaleRepository;
    @Mock
    private AnswerService answerService;
    @Mock
    private ExplanationQuestionService explanationQuestionService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private QuestionServiceImpl questionService;

    // ===== Shared test data =====
    private User teacherUser;
    private QuestionBank questionBank;
    private Question question;
    private BankResponse bankResponse;
    private QuestionResponse questionResponse;

    @BeforeEach
    void setUp() {
        teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setEmail("teacher@test.com");

        questionBank = QuestionBank.builder()
                .id(1L).title("Grammar Bank").createBy(teacherUser).build();

        question = Question.builder()
                .id(1L).content("What is a noun?").questionBank(questionBank).createBy(teacherUser).build();

        bankResponse = BankResponse.builder()
                .id(1L).questionBankTitle("Grammar Bank").build();

        questionResponse = QuestionResponse.builder()
                .id(1L).questionContent("What is a noun?").build();
    }

    // =====================================================================
    // getDetail tests
    // =====================================================================
    @Nested
    @DisplayName("getDetail - Lấy chi tiết câu hỏi")
    class GetDetailTests {

        /**
         * UT_QST_009: Lấy chi tiết câu hỏi thành công theo ID hợp lệ.
         */
        @Test
        @DisplayName("TC_QST_009 - Lấy chi tiết câu hỏi thành công")
        void getDetail_WithValidId_ShouldReturnQuestionResponse() {
            // Arrange
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(convertUtil.convertQuestionToDto(question)).thenReturn(questionResponse);

            // Act
            QuestionResponse result = questionService.getDetail(httpServletRequest, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getQuestionContent()).isEqualTo("What is a noun?");
            verify(questionRepository).findById(1L);
        }

        /**
         * UT_QST_010: Lấy chi tiết câu hỏi thất bại khi ID không tồn tại.
         */
        @Test
        @DisplayName("TC_QST_010 - Lấy chi tiết câu hỏi thất bại khi không tồn tại")
        void getDetail_WithNonExistentId_ShouldThrowException() {
            // Arrange
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionService.getDetail(httpServletRequest, 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUESTION);
                    });
        }
    }

    // =====================================================================
    // addQuestionToBank tests
    // =====================================================================
    @Nested
    @DisplayName("addQuestionToBank - Thêm câu hỏi vào ngân hàng đề")
    class AddQuestionToBankTests {

        private QuestionRequest buildValidRequest() {
            AnswerRequest a1 = new AnswerRequest();
            a1.setContent("Answer A");
            a1.setCorrect(true);
            AnswerRequest a2 = new AnswerRequest();
            a2.setContent("Answer B");
            a2.setCorrect(false);
            AnswerRequest a3 = new AnswerRequest();
            a3.setContent("Answer C");
            a3.setCorrect(false);
            AnswerRequest a4 = new AnswerRequest();
            a4.setContent("Answer D");
            a4.setCorrect(false);

            ExplanationQuestionRequest explanation = new ExplanationQuestionRequest();
            explanation.setExplanationVietnamese("Giải thích TV");
            explanation.setExplanationEnglish("Explanation EN");

            QuestionRequest request = new QuestionRequest();
            request.setQuestionContent("What is a noun?");
            request.setCategory("NOUN - Danh từ");
            request.setDifficulty("Muc2(6-10)");
            request.setAnswers(List.of(a1, a2, a3, a4));
            request.setExplanation(explanation);
            return request;
        }

        /**
         * UT_QST_001: Thêm câu hỏi hợp lệ vào bank thành công.
         */
        @Test
        @DisplayName("TC_QST_001 - Thêm câu hỏi hợp lệ vào bank thành công")
        void addQuestionToBank_WithValidData_ShouldSaveAllEntities() {
            // Arrange
            QuestionRequest request = buildValidRequest();
            RangeTopic rangeTopic = new RangeTopic();
            rangeTopic.setContent("NOUN - Danh từ");
            ScoreScale scoreScale = new ScoreScale();
            scoreScale.setTitle("Muc2(6-10)");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(rangeTopicRepository.findByContent("NOUN - Danh từ")).thenReturn(rangeTopic);
            when(scoreScaleRepository.findByTitle("Muc2(6-10)")).thenReturn(scoreScale);
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(explanationQuestionRepository.save(any(ExplanationQuestion.class)))
                    .thenReturn(new ExplanationQuestion());
            when(answerRepository.save(any(Answer.class))).thenReturn(new Answer());
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            BankResponse result = questionService.addQuestionToBank(httpServletRequest, request, 1L);

            // Assert
            assertThat(result).isNotNull();

            // CheckDB: question, explanation, và 4 answers đều được lưu
            verify(questionRepository).save(any(Question.class));
            verify(explanationQuestionRepository).save(any(ExplanationQuestion.class));
            verify(answerRepository, times(4)).save(any(Answer.class));
        }

        /**
         * UT_QST_002: Nội dung câu hỏi toàn khoảng trắng → KHÔNG được lưu.
         * Mapped: TC_FN_020 Fail (bug – system vẫn lưu whitespace)
         */
        @Test
        @DisplayName("TC_QST_002 - Nội dung câu hỏi toàn khoảng trắng KHÔNG được lưu")
        void addQuestionToBank_WithWhiteSpaceContent_ShouldNotPersist() {
            // Arrange
            QuestionRequest request = new QuestionRequest();
            request.setQuestionContent("         "); // toàn khoảng trắng
            request.setExplanation(new ExplanationQuestionRequest()); // tránh NPE trong service
            request.setAnswers(Collections.emptyList()); // tránh NPE khi loop answers

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(explanationQuestionRepository.save(any())).thenReturn(new ExplanationQuestion());

            // Act
            questionService.addQuestionToBank(httpServletRequest, request, 1L);

            // Assert: CheckDB – không lưu câu hỏi với content rỗng
            verify(questionRepository, never()).save(any());
            verify(answerRepository, never()).save(any());
        }

        /**
         * UT_QST_003: Answers toàn khoảng trắng → KHÔNG được lưu.
         * Mapped: TC_FN_022 Fail (bug)
         */
        @Test
        @DisplayName("TC_QST_003 - Answer toàn khoảng trắng KHÔNG được lưu")
        void addQuestionToBank_WithWhiteSpaceAnswers_ShouldNotPersist() {
            // Arrange
            AnswerRequest blankAnswer = new AnswerRequest();
            blankAnswer.setContent("       ");
            blankAnswer.setCorrect(false);

            QuestionRequest request = new QuestionRequest();
            request.setQuestionContent("What is a noun?");
            request.setAnswers(List.of(blankAnswer, blankAnswer, blankAnswer, blankAnswer));
            request.setExplanation(new ExplanationQuestionRequest());

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(explanationQuestionRepository.save(any())).thenReturn(new ExplanationQuestion());

            // Act
            questionService.addQuestionToBank(httpServletRequest, request, 1L);

            // Assert: CheckDB – không lưu answer rỗng
            verify(answerRepository, never()).save(any());
        }

        /**
         * UT_QST_004: Explanation toàn khoảng trắng → KHÔNG được lưu.
         * Mapped: TC_FN_025 Fail (bug)
         */
        @Test
        @DisplayName("TC_QST_004 - Explanation toàn khoảng trắng KHÔNG được lưu")
        void addQuestionToBank_WithWhiteSpaceExplanation_ShouldNotPersist() {
            // Arrange
            ExplanationQuestionRequest blankExp = new ExplanationQuestionRequest();
            blankExp.setExplanationVietnamese("      ");
            blankExp.setExplanationEnglish("      ");

            QuestionRequest request = buildValidRequest();
            request.setExplanation(blankExp);

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));

            // Act
            questionService.addQuestionToBank(httpServletRequest, request, 1L);

            // Assert: CheckDB – không lưu explanation rỗng
            verify(explanationQuestionRepository, never()).save(any());
        }

        /**
         * UT_QST_005: Thêm câu hỏi thất bại khi bank không tồn tại.
         */
        @Test
        @DisplayName("TC_QST_005 - Thêm câu hỏi thất bại khi bank không tồn tại")
        void addQuestionToBank_WithNonExistentBank_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionService.addQuestionToBank(httpServletRequest, new QuestionRequest(), 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.BANK);
                    });

            // CheckDB: không lưu khi bank không tồn tại
            verify(questionRepository, never()).save(any());
        }

        /**
         * UT_QST_006: Thêm câu hỏi thất bại khi user không tồn tại.
         */
        @Test
        @DisplayName("TC_QST_006 - Thêm câu hỏi thất bại khi user không tồn tại")
        void addQuestionToBank_WithUnknownUser_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("unknown@test.com");
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionService.addQuestionToBank(httpServletRequest, new QuestionRequest(), 1L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.USER);
                    });
        }
    }

    // =====================================================================
    // removeQuestionFromBank tests
    // =====================================================================
    @Nested
    @DisplayName("removeQuestionFromBank - Xóa câu hỏi khỏi ngân hàng đề")
    class RemoveQuestionFromBankTests {

        /**
         * UT_QST_007: Xóa câu hỏi khỏi bank thành công (soft delete).
         * Mapped: TC_FN_028
         */
        @Test
        @DisplayName("TC_QST_007 - Xóa câu hỏi khỏi bank thành công (soft delete)")
        void removeQuestionFromBank_WithValidIds_ShouldSoftDelete() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            BankResponse result = questionService.removeQuestionFromBank(
                    httpServletRequest, List.of(1L), 1L);

            // Assert
            assertThat(result).isNotNull();

            // CheckDB: isDelete=true được set và save được gọi
            verify(questionRepository).save(argThat(q -> q.getIsDelete() == Boolean.TRUE));
        }

        /**
         * UT_QST_008: Xóa câu hỏi thất bại khi câu hỏi không tồn tại.
         */
        @Test
        @DisplayName("TC_QST_008 - Xóa câu hỏi thất bại khi câu hỏi không tồn tại")
        void removeQuestionFromBank_WithNonExistentQuestion_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(questionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionService.removeQuestionFromBank(httpServletRequest, List.of(999L), 1L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.QUESTION);
                    });
        }
    }

    // =====================================================================
    // saveQuestion (AI result) tests
    // =====================================================================
    @Nested
    @DisplayName("saveQuestion - Lưu kết quả phân tích AI vào bank")
    class SaveQuestionTests {

        /**
         * UT_QST_013: Lưu danh sách câu hỏi từ AI vào bank mới thành công.
         * Mapped: TC_FN_034, TC_FN_037
         */
        @Test
        @DisplayName("UT_QST_011 - Lưu câu hỏi AI vào bank mới thành công")
        void saveQuestion_WithValidAiResponse_ShouldSaveAllEntities() {
            // Arrange
            AnswerResponse ans1 = new AnswerResponse();
            ans1.setContent("A");
            ans1.setCorrect(true);
            AnswerResponse ans2 = new AnswerResponse();
            ans2.setContent("B");
            ans2.setCorrect(false);

            QuestionResponse qResp = QuestionResponse.builder()
                    .questionContent("Sample MCQ question")
                    .category("NOUN - Danh từ")
                    .difficulty("Muc2(6-10)")
                    .answers(List.of(ans1, ans2))
                    .explanation(new ExplanationQuestionResponse())
                    .build();

            AiResponse aiResponse = AiResponse.builder()
                    .questionBankTitle("AI Generated Bank")
                    .url("http://example.com/input.pdf")
                    .questions(List.of(qResp))
                    .build();

            RangeTopic rangeTopic = new RangeTopic();
            rangeTopic.setContent("NOUN - Danh từ");
            ScoreScale scoreScale = new ScoreScale();
            scoreScale.setTitle("Muc2(6-10)");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.save(any(QuestionBank.class))).thenReturn(questionBank);
            when(rangeTopicRepository.findByContent("NOUN - Danh từ")).thenReturn(rangeTopic);
            when(scoreScaleRepository.findByTitle("Muc2(6-10)")).thenReturn(scoreScale);
            when(questionRepository.save(any(Question.class))).thenReturn(question);
            when(explanationQuestionRepository.save(any(ExplanationQuestion.class)))
                    .thenReturn(new ExplanationQuestion());
            when(answerRepository.save(any(Answer.class))).thenReturn(new Answer());
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            BankResponse result = questionService.saveQuestion(httpServletRequest, aiResponse);

            // Assert
            assertThat(result).isNotNull();

            // CheckDB: bank, question, explanation, và 2 answers đều được lưu
            verify(questionBankRepository).save(any(QuestionBank.class));
            verify(questionRepository).save(any(Question.class));
            verify(explanationQuestionRepository).save(any(ExplanationQuestion.class));
            verify(answerRepository, times(2)).save(any(Answer.class));
        }

        /**
         * UT_QST_014: Lưu thất bại khi user không tồn tại.
         */
        @Test
        @DisplayName("UT_QST_012 - Lưu câu hỏi AI thất bại khi user không tồn tại")
        void saveQuestion_WithUnknownUser_ShouldThrowException() {
            // Arrange
            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("unknown@test.com");
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionService.saveQuestion(httpServletRequest, new AiResponse()))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.USER);
                    });

            // CheckDB: không lưu gì khi user không tồn tại
            verify(questionBankRepository, never()).save(any());
            verify(questionRepository, never()).save(any());
        }
    }
}
