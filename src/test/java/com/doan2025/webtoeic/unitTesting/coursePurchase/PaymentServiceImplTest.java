package com.doan2025.webtoeic.unitTesting.coursePurchase;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.view.RedirectView;

import com.doan2025.webtoeic.constants.enums.EStatusOrder;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.domain.Course;
import com.doan2025.webtoeic.domain.OrderDetail;
import com.doan2025.webtoeic.domain.Orders;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.response.PaymentResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.EnrollmentRepository;
import com.doan2025.webtoeic.repository.OrderDetailRepository;
import com.doan2025.webtoeic.repository.OrderRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.service.impl.PaymentServiceImpl;
import com.doan2025.webtoeic.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setEmail("student@gmail.com");

        course = Course.builder().id(22L).title("TOEIC Listening").price(499_000L).build();

        ReflectionTestUtils.setField(paymentService, "FE", "http://localhost:5173");
        ReflectionTestUtils.setField(paymentService, "BE", "http://localhost:8888/");
        ReflectionTestUtils.setField(paymentService, "VNP_RETURN_URL", "");
        ReflectionTestUtils.setField(paymentService, "MOCK_PAYMENT_ENABLED", true);
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_001_shouldReturnMockCallbackUrlWhenMockEnabled() {
        // TC-UT-PAY-001: Ở chế độ mock, hệ thống phải trả URL callback thành công để FE test luồng tiếp theo.
        Orders pendingOrder = Orders.builder()
                .id(700L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .id(701L)
                .orders(pendingOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(pendingOrder.getId())).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(pendingOrder.getId())).thenReturn(Optional.of(orderDetail));

        PaymentResponse response = paymentService.createVNPayPayment(pendingOrder.getId(), request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertTrue(response.getURL().contains("vnp_ResponseCode=00"));
        assertTrue(response.getURL().contains("vnp_TxnRef="));
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_002_shouldMarkOrderCompletedAndCreateEnrollment() {
        // TC-UT-PAY-002: Callback success phải cập nhật order COMPLETED và tạo enrollment nếu chưa có.
        Orders pendingOrder = Orders.builder()
                .id(801L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(802L)
                .orders(pendingOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getParameter("vnp_TxnRef")).thenReturn("20260413120000_801");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("123456");
        when(request.getParameter("vnp_Amount")).thenReturn("49900000");
        when(request.getParameter("vnp_PayDate")).thenReturn("20260413120500");

        when(orderRepository.findById(801L)).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(801L)).thenReturn(Optional.of(detail));
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("/order-status?status=success"));
        assertEquals(EStatusOrder.COMPLETED, pendingOrder.getStatus());

        verify(orderRepository).save(pendingOrder);
        ArgumentCaptor<com.doan2025.webtoeic.domain.Enrollment> enrollmentCaptor =
                ArgumentCaptor.forClass(com.doan2025.webtoeic.domain.Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        assertEquals(student, enrollmentCaptor.getValue().getUser());
        assertEquals(course, enrollmentCaptor.getValue().getCourse());
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_003_shouldNotCreateDuplicateEnrollmentForCompletedOrder() {
        // TC-UT-PAY-003: Callback success lặp lại không được tạo enrollment/order update trùng.
        Orders completedOrder = Orders.builder()
                .id(901L)
                .user(student)
                .status(EStatusOrder.COMPLETED)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(902L)
                .orders(completedOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getParameter("vnp_TxnRef")).thenReturn("20260413120000_901");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("223344");
        when(request.getParameter("vnp_Amount")).thenReturn("49900000");
        when(request.getParameter("vnp_PayDate")).thenReturn("20260413121000");

        when(orderRepository.findById(901L)).thenReturn(Optional.of(completedOrder));
        when(orderDetailRepository.findByOrderId(901L)).thenReturn(Optional.of(detail));
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(true);

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("/order-status?status=success"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_004_shouldRedirectFailWhenTxnRefIsInvalid() {
        // TC-UT-PAY-004: TxnRef sai định dạng phải chuyển fail và không tác động DB.
        when(request.getParameter("vnp_TxnRef")).thenReturn("invalid_txn");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertEquals("http://localhost:5173/order-status?status=fail", redirectView.getUrl());
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_005_shouldRedirectToFailWhenResponseCodeIsNotSuccess() {
        // TC-UT-PAY-005: Callback với mã lỗi khác "00" phải redirect về fail mà không cập nhật DB.
        when(request.getParameter("vnp_TxnRef")).thenReturn(null);
        when(request.getParameter("vnp_ResponseCode")).thenReturn("07");

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("/order-status?status=fail"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_006_shouldThrowHasPaidWhenOrderAlreadyCompleted() {
        // TC-UT-PAY-006: Không được tạo link thanh toán nếu order đã ở trạng thái COMPLETED.
        Orders completedOrder = Orders.builder()
                .id(702L)
                .user(student)
                .status(EStatusOrder.COMPLETED)
                .totalAmount(499_000L)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(703L)
                .orders(completedOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(702L)).thenReturn(Optional.of(completedOrder));
        when(orderDetailRepository.findByOrderId(702L)).thenReturn(Optional.of(detail));

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(702L, request));

        assertEquals(ResponseCode.HAS_PAID, ex.getResponseCode());
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_007_shouldThrowNotPermissionWhenUserIsNotOrderOwner() {
        // TC-UT-PAY-007: Không được thanh toán order của người dùng khác.
        User owner = new User();
        owner.setId(99L);
        owner.setEmail("other@gmail.com");

        Orders ownerOrder = Orders.builder()
                .id(800L)
                .user(owner)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(800L)).thenReturn(Optional.of(ownerOrder));

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(800L, request));

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        verify(orderDetailRepository, never()).findByOrderId(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_008_shouldBuildRealVNPayUrlWhenMockDisabled() {
        // TC-UT-PAY-008: Khi mock=false, phải xây dựng URL VNPay thực với đủ tham số và HMAC hash.
        ReflectionTestUtils.setField(paymentService, "MOCK_PAYMENT_ENABLED", false);
        ReflectionTestUtils.setField(paymentService, "SECRET_KEY", "test-secret-key-for-unit-testing");
        ReflectionTestUtils.setField(paymentService, "ORDER_TYPE", "other");
        ReflectionTestUtils.setField(paymentService, "VPN_COMMAND", "pay");
        ReflectionTestUtils.setField(paymentService, "VPN_VERSION", "2.1.0");
        ReflectionTestUtils.setField(paymentService, "VPN_PAY_URL", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(paymentService, "VPN_TMN_CODE", "TESTCODE01");

        Orders pendingOrder = Orders.builder()
                .id(850L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(851L)
                .orders(pendingOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(850L)).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(850L)).thenReturn(Optional.of(detail));
        when(request.getHeader("X-FORWARDED-FOR")).thenReturn("127.0.0.1");

        PaymentResponse response = paymentService.createVNPayPayment(850L, request);

        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertTrue(response.getURL().contains("vnp_TmnCode=TESTCODE01"));
        assertTrue(response.getURL().contains("vnp_SecureHash"));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_009_shouldThrowUserNotExistedWhenAuthorizationHeaderMissing() {
        // TC-UT-PAY-009: Thiếu Authorization thì không trích xuất được email và phải báo USER không tồn tại.
        when(request.getHeader("Authorization")).thenReturn(null);
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(700L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).findById(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_010_shouldThrowUserNotExistedWhenAuthorizationHeaderIsNotBearer() {
        // TC-UT-PAY-010: Authorization sai prefix cũng phải đi nhánh không đọc JWT.
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(700L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).findById(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_011_shouldThrowInvalidWhenAmountIsNegative() {
        // TC-UT-PAY-011: Tổng tiền âm phải bị chặn ở nhánh validation amount.
        Orders invalidOrder = Orders.builder()
                .id(711L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(-1L)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(712L)
                .orders(invalidOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(711L)).thenReturn(Optional.of(invalidOrder));
        when(orderDetailRepository.findByOrderId(711L)).thenReturn(Optional.of(detail));

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(711L, request));

        assertEquals(ResponseCode.INVALID, ex.getResponseCode());
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_012_shouldUseConfiguredReturnUrlAndSkipNullOrEmptyParams() {
        // TC-UT-PAY-012: Khi cấu hình return URL riêng và có param null/rỗng, URL tạo ra vẫn hợp lệ và bỏ qua param đó.
        ReflectionTestUtils.setField(paymentService, "MOCK_PAYMENT_ENABLED", false);
        ReflectionTestUtils.setField(paymentService, "SECRET_KEY", "test-secret-key-for-unit-testing");
        ReflectionTestUtils.setField(paymentService, "ORDER_TYPE", "");
        ReflectionTestUtils.setField(paymentService, "VPN_COMMAND", "pay");
        ReflectionTestUtils.setField(paymentService, "VPN_VERSION", "2.1.0");
        ReflectionTestUtils.setField(paymentService, "VPN_PAY_URL", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(paymentService, "VPN_TMN_CODE", null);
        ReflectionTestUtils.setField(paymentService, "VNP_RETURN_URL", "https://example.test/payment/return");

        Orders pendingOrder = Orders.builder()
                .id(860L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(861L)
                .orders(pendingOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(860L)).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(860L)).thenReturn(Optional.of(detail));
        when(request.getHeader("X-FORWARDED-FOR")).thenReturn("127.0.0.1");

        PaymentResponse response = paymentService.createVNPayPayment(860L, request);

        assertEquals("success", response.getStatus());
        assertTrue(response.getURL().contains("vnp_ReturnUrl=https%3A%2F%2Fexample.test%2Fpayment%2Freturn"));
        assertTrue(!response.getURL().contains("vnp_OrderType"));
        assertTrue(!response.getURL().contains("vnp_TmnCode"));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_013_shouldUseConfiguredReturnUrlInMockMode() {
        // TC-UT-PAY-013: Ở mock mode, callback URL cũng phải ưu tiên VNP_RETURN_URL nếu được cấu hình.
        ReflectionTestUtils.setField(paymentService, "VNP_RETURN_URL", "https://example.test/payment/mock-return");

        Orders pendingOrder = Orders.builder()
                .id(870L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .id(871L)
                .orders(pendingOrder)
                .course(course)
                .priceAtPurchase(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(pendingOrder.getId())).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(pendingOrder.getId())).thenReturn(Optional.of(orderDetail));

        PaymentResponse response = paymentService.createVNPayPayment(pendingOrder.getId(), request);

        assertEquals("success", response.getStatus());
        assertTrue(response.getURL().startsWith("https://example.test/payment/mock-return?"));
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_014_shouldRedirectFailWhenTxnRefIsNull() {
        // TC-UT-PAY-014: TxnRef null phải rơi vào nhánh fail của extractOrderId.
        when(request.getParameter("vnp_TxnRef")).thenReturn(null);
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertEquals("http://localhost:5173/order-status?status=fail", redirectView.getUrl());
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    @Test
        void handleVNPayReturn_TC_UT_PAY_015_specSaysCancelledOrderCannotBeCompletedByCallback() {
                // TC-UT-PAY-015: Spec STP_PAY_023 cho rằng success callback chỉ thiết lập PENDING orders
        // để COMPLETED. Nó không nên apply cho CANCELLED orders.
        // Code không kiểm tra status là PENDING trước, nó chỉ kiểm tra != COMPLETED.
        // => CANCELLED order sẽ được đổi thành COMPLETED (sai spec)
        // => TEST NÀY SỂ FAIL.
        Orders cancelledOrder = Orders.builder()
                .id(950L)
                .user(student)
                .status(EStatusOrder.CANCELLED)
                .totalAmount(699_000L)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(951L)
                .orders(cancelledOrder)
                .course(course)
                .build();

        when(request.getParameter("vnp_TxnRef")).thenReturn("20260415143000_950");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("123456789");
        when(request.getParameter("vnp_Amount")).thenReturn("69900000");
        when(request.getParameter("vnp_PayDate")).thenReturn("20260415143030");
        when(orderRepository.findById(950L)).thenReturn(Optional.of(cancelledOrder));
        when(orderDetailRepository.findByOrderId(950L)).thenReturn(Optional.of(detail));
        when(enrollmentRepository.existsByUserAndCourse(student, course)).thenReturn(false);

        paymentService.handleVNPayReturn(request);

        // Spec: CANCELLED orders không nên được update, save never called
        // Actual: code call save với status = COMPLETED (do setStatus)
        // Test: assert save NEVER được gọi, nhưng code gọi nó anyway → FAIL
        verify(orderRepository, never()).save(any(Orders.class));
    }
}
