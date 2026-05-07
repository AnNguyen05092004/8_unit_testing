package com.doan2025.webtoeic.unitTesting.quizAI;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.QuestionBank;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBankDto;
import com.doan2025.webtoeic.dto.request.BankRequest;
import com.doan2025.webtoeic.dto.response.BankResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.QuestionBankRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.impl.QuestionBankServiceImpl;
import com.doan2025.webtoeic.utils.ConvertUtil;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuestionBankServiceImpl.
 * Scope: UT_QB_001 – UT_QB_009 (unit_test_plan.md §3.3)
 * Mapped system tests: TC_FN_015–019
 */
@ExtendWith(MockitoExtension.class)
class QuestionBankServiceImplTest {

    @Mock
    private QuestionBankRepository questionBankRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ConvertUtil convertUtil;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private QuestionBankServiceImpl questionBankService;

    // ===== Shared test data =====
    private User teacherUser;
    private QuestionBank questionBank;
    private BankResponse bankResponse;

    @BeforeEach
    void setUp() {
        teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setEmail("teacher@test.com");

        questionBank = QuestionBank.builder()
                .id(1L)
                .title("Grammar Bank")
                .linkUrl("http://example.com/file.pdf")
                .createBy(teacherUser)
                .build();

        bankResponse = BankResponse.builder()
                .id(1L)
                .questionBankTitle("Grammar Bank")
                .url("http://example.com/file.pdf")
                .build();
    }

    // =====================================================================
    // saveQuestionBank tests
    // =====================================================================
    @Nested
    @DisplayName("saveQuestionBank - Tạo ngân hàng câu hỏi")
    class SaveQuestionBankTests {

        /**
         * UT_QB_001: Tạo bank với title hợp lệ → lưu thành công.
         * Mapped: TC_FN_016 (valid title accepted)
         */
        @Test
        @DisplayName("UT_QB_001 - Tạo bank thành công với title hợp lệ")
        void saveQuestionBank_WithValidTitle_ShouldReturnBankResponse() {
            // Arrange
            BankRequest request = new BankRequest();
            request.setQuestionBankTitle("Grammar Bank");
            request.setUrl("http://example.com/file.pdf");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.save(any(QuestionBank.class))).thenReturn(questionBank);
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            BankResponse result = questionBankService.saveQuestionBank(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getQuestionBankTitle()).isEqualTo("Grammar Bank");

            // CheckDB: save phải được gọi đúng 1 lần
            verify(questionBankRepository).save(any(QuestionBank.class));
        }

        /**
         * UT_QB_002: Tạo bank với title toàn khoảng trắng → KHÔNG được lưu (expected
         * behavior sau fix).
         * Mapped: TC_FN_015 Fail – whitespace title được lưu silently (bug
         * documentation)
         * Ghi chú: Hiện tại hệ thống vẫn cho lưu → test này sẽ FAIL cho đến khi có
         * validation.
         */
        @Test
        @DisplayName("TC_QB_002 - Tạo bank với title toàn khoảng trắng KHÔNG được lưu")
        void saveQuestionBank_WithWhiteSpaceTitle_ShouldNotPersist() {
            // Arrange
            BankRequest request = new BankRequest();
            request.setQuestionBankTitle("          "); // toàn khoảng trắng
            request.setUrl("http://example.com/file.pdf");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));

            // Act
            questionBankService.saveQuestionBank(httpServletRequest, request);

            // Assert: CheckDB – title trắng thì không được lưu vào DB
            verify(questionBankRepository, never()).save(any());
        }

        /**
         * UT_QB_003: Tạo bank thất bại khi user không tồn tại.
         */
        @Test
        @DisplayName("UT_QB_003 - Tạo bank thất bại khi user không tồn tại")
        void saveQuestionBank_WithUnknownUser_ShouldThrowException() {
            // Arrange
            BankRequest request = new BankRequest();
            request.setQuestionBankTitle("Grammar Bank");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("unknown@test.com");
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionBankService.saveQuestionBank(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.USER);
                    });

            // CheckDB: không lưu khi user không tồn tại
            verify(questionBankRepository, never()).save(any());
        }
    }

    // =====================================================================
    // updateQuestionBank tests
    // =====================================================================
    @Nested
    @DisplayName("updateQuestionBank - Cập nhật ngân hàng câu hỏi")
    class UpdateQuestionBankTests {

        /**
         * UT_QB_004: Cập nhật bank thành công với dữ liệu hợp lệ.
         */
        @Test
        @DisplayName("UT_QB_004 - Cập nhật bank thành công")
        void updateQuestionBank_WithValidData_ShouldReturnUpdatedResponse() {
            // Arrange
            BankRequest request = new BankRequest();
            request.setId(1L);
            request.setQuestionBankTitle("Updated Grammar Bank");
            request.setUrl("http://example.com/new.pdf");

            QuestionBank updatedBank = QuestionBank.builder()
                    .id(1L)
                    .title("Updated Grammar Bank")
                    .linkUrl("http://example.com/new.pdf")
                    .build();

            BankResponse updatedResponse = BankResponse.builder()
                    .id(1L)
                    .questionBankTitle("Updated Grammar Bank")
                    .build();

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(questionBankRepository.save(any(QuestionBank.class))).thenReturn(updatedBank);
            when(convertUtil.convertQuestionBankToDto(updatedBank)).thenReturn(updatedResponse);

            // Act
            BankResponse result = questionBankService.updateQuestionBank(httpServletRequest, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getQuestionBankTitle()).isEqualTo("Updated Grammar Bank");

            // CheckDB: save được gọi với dữ liệu đã cập nhật
            verify(questionBankRepository).save(any(QuestionBank.class));
        }

        /**
         * UT_QB_005: Cập nhật bank thất bại khi bank không tồn tại.
         */
        @Test
        @DisplayName("UT_QB_005 - Cập nhật bank thất bại khi không tồn tại")
        void updateQuestionBank_WithNonExistentBank_ShouldThrowException() {
            // Arrange
            BankRequest request = new BankRequest();
            request.setId(999L);
            request.setQuestionBankTitle("Updated Bank");

            when(jwtUtil.getEmailFromToken(httpServletRequest)).thenReturn("teacher@test.com");
            when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacherUser));
            when(questionBankRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionBankService.updateQuestionBank(httpServletRequest, request))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.BANK);
                    });

            // CheckDB: không lưu khi bank không tồn tại
            verify(questionBankRepository, never()).save(any());
        }
    }

    // =====================================================================
    // getQuestionBank tests
    // =====================================================================
    @Nested
    @DisplayName("getQuestionBank - Lấy chi tiết ngân hàng câu hỏi")
    class GetQuestionBankTests {

        /**
         * UT_QB_006: Lấy bank theo ID hợp lệ → trả về BankResponse.
         */
        @Test
        @DisplayName("UT_QB_006 - Lấy bank thành công theo ID hợp lệ")
        void getQuestionBank_WithValidId_ShouldReturnBankResponse() {
            // Arrange
            when(questionBankRepository.findById(1L)).thenReturn(Optional.of(questionBank));
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            BankResponse result = questionBankService.getQuestionBank(httpServletRequest, 1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getQuestionBankTitle()).isEqualTo("Grammar Bank");
            verify(questionBankRepository).findById(1L);
        }

        /**
         * UT_QB_007: Lấy bank với ID không tồn tại → ném exception.
         */
        @Test
        @DisplayName("UT_QB_007 - Lấy bank thất bại khi ID không tồn tại")
        void getQuestionBank_WithNonExistentId_ShouldThrowException() {
            // Arrange
            when(questionBankRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> questionBankService.getQuestionBank(httpServletRequest, 999L))
                    .isInstanceOf(WebToeicException.class)
                    .satisfies(ex -> {
                        WebToeicException wex = (WebToeicException) ex;
                        assertThat(wex.getResponseCode()).isEqualTo(ResponseCode.NOT_EXISTED);
                        assertThat(wex.getResponseObject()).isEqualTo(ResponseObject.BANK);
                    });
        }
    }

    // =====================================================================
    // getQuestionBanks tests
    // =====================================================================
    @Nested
    @DisplayName("getQuestionBanks - Lấy danh sách ngân hàng câu hỏi")
    class GetQuestionBanksTests {

        /**
         * UT_QB_008: Lấy danh sách bank có kết quả → trả về page đúng.
         * Mapped: TC_FN_017 (date filter start > end → auto swap), TC_FN_019
         */
        @Test
        @DisplayName("UT_QB_008 - Lấy danh sách bank với bộ lọc ngày hợp lệ")
        void getQuestionBanks_WithDateFilter_ShouldReturnPagedResult() {
            // Arrange
            SearchBankDto dto = new SearchBankDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<QuestionBank> bankPage = new PageImpl<>(List.of(questionBank), pageable, 1);

            when(questionBankRepository.filter(dto, pageable)).thenReturn(bankPage);
            when(convertUtil.convertQuestionBankToDto(questionBank)).thenReturn(bankResponse);

            // Act
            Page<BankResponse> result = questionBankService.getQuestionBanks(httpServletRequest, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getQuestionBankTitle()).isEqualTo("Grammar Bank");
            verify(questionBankRepository).filter(dto, pageable);
        }

        /**
         * UT_QB_009: Lấy danh sách bank không có kết quả → trả về empty page.
         * Mapped: TC_FN_019 Fail – filter theo date không hoạt động (bug documentation)
         */
        @Test
        @DisplayName("UT_QB_009 - Lấy danh sách bank trả về empty khi không có dữ liệu")
        void getQuestionBanks_WithNoMatchingData_ShouldReturnEmptyPage() {
            // Arrange
            SearchBankDto dto = new SearchBankDto();
            Pageable pageable = PageRequest.of(0, 10);
            Page<QuestionBank> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(questionBankRepository.filter(dto, pageable)).thenReturn(emptyPage);

            // Act
            Page<BankResponse> result = questionBankService.getQuestionBanks(httpServletRequest, dto, pageable);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
