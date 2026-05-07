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

    // Cac dependency duoc mock de test service theo tung nhanh logic,
    // khong goi DB/network that su.

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
        // Du lieu nen chung cho nhieu test.
        student = new User();
        student.setId(1L);
        student.setEmail("student@gmail.com");

        course = Course.builder().id(22L).title("TOEIC Listening").price(499_000L).build();

        // Set cac field private trong PaymentServiceImpl de test chay on dinh,
        // khong phu thuoc vao env file ben ngoai.
        ReflectionTestUtils.setField(paymentService, "FE", "http://localhost:5173");
        ReflectionTestUtils.setField(paymentService, "BE", "http://localhost:8888/");
        ReflectionTestUtils.setField(paymentService, "VNP_RETURN_URL", "");
        ReflectionTestUtils.setField(paymentService, "MOCK_PAYMENT_ENABLED", true);
    }

    // =========================================================================
    // createVNPayPayment
    // =========================================================================

    @Test
    void createVNPayPayment_TC_UT_PAY_001_shouldReturnMockCallbackUrlWhenMockEnabled() {
        // TC-UT-PAY-001
        // Arrange: tao order PENDING + orderDetail hop le.
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

        // Act: goi ham can test.
        PaymentResponse response = paymentService.createVNPayPayment(pendingOrder.getId(), request);

        // Assert: mock mode tra URL callback success.
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertTrue(response.getURL().contains("vnp_ResponseCode=00"));
        assertTrue(response.getURL().contains("vnp_TxnRef="));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_002_shouldBuildRealVNPayUrlWhenMockDisabled() {
        // TC-UT-PAY-002
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
    void createVNPayPayment_TC_UT_PAY_003_shouldUseConfiguredReturnUrlAndSkipNullOrEmptyParams() {
        // TC-UT-PAY-003
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
    void createVNPayPayment_TC_UT_PAY_004_shouldUseConfiguredReturnUrlInMockMode() {
        // TC-UT-PAY-004
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
    void createVNPayPayment_TC_UT_PAY_005_shouldThrowHasPaidWhenOrderAlreadyCompleted() {
        // TC-UT-PAY-005
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
    void createVNPayPayment_TC_UT_PAY_006_shouldThrowNotPermissionWhenUserIsNotOrderOwner() {
        // TC-UT-PAY-006
        // Arrange: tao order thuoc user khac.
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

        // Act + Assert: service phai chan va nem NOT_PERMISSION.
        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(800L, request));

        assertEquals(ResponseCode.NOT_PERMISSION, ex.getResponseCode());
        // Khi sai owner thi khong can query orderDetail nua.
        verify(orderDetailRepository, never()).findByOrderId(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_007_shouldThrowInvalidWhenAmountIsNegative() {
        // TC-UT-PAY-007
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
    void createVNPayPayment_TC_UT_PAY_008_shouldThrowUserNotExistedWhenAuthorizationHeaderMissing() {
        // TC-UT-PAY-008
        // Arrange: khong co Authorization header.
        when(request.getHeader("Authorization")).thenReturn(null);
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // Act + Assert: parse user that bai -> NOT_EXISTED.
        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(700L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        // User khong hop le thi khong duoc query order.
        verify(orderRepository, never()).findById(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_009_shouldThrowUserNotExistedWhenAuthorizationHeaderIsNotBearer() {
        // TC-UT-PAY-009
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(700L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).findById(any(Long.class));
    }

    @Test
    void createVNPayPayment_TC_UT_PAY_010_shouldThrowWhenOrderNotFound() {
        // TC-UT-PAY-010
        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(9999L)).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(9999L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        verify(orderDetailRepository, never()).findByOrderId(any(Long.class));
    }

    
        @Test
    void createVNPayPayment_TC_UT_PAY_011_shouldThrowWhenOrderDetailNotFound() {
        // TC-UT-PAY-011: Order tìm thấy và đúng owner, status PENDING, amount hợp lệ,
        // nhưng orderDetail không tồn tại → phải ném NOT_EXISTED.
        Orders pendingOrder = Orders.builder()
                .id(720L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .totalAmount(499_000L)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer fake-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn(student.getEmail());
        when(userRepository.findByEmail(student.getEmail())).thenReturn(Optional.of(student));
        when(orderRepository.findById(720L)).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(720L)).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(720L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
    }

    
        @Test
    void createVNPayPayment_TC_UT_PAY_012_shouldThrowWhenBearerTokenIsValidButUserNotFound() {
        // TC-UT-PAY-012: Header Bearer hợp lệ, parse được email nhưng user không tồn tại.
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.getEmailFromToken(request)).thenReturn("ghost@gmail.com");
        when(userRepository.findByEmail("ghost@gmail.com")).thenReturn(Optional.empty());

        WebToeicException ex = assertThrows(WebToeicException.class,
                () -> paymentService.createVNPayPayment(700L, request));

        assertEquals(ResponseCode.NOT_EXISTED, ex.getResponseCode());
        verify(orderRepository, never()).findById(any(Long.class));
    }

    // =========================================================================
    // handleVNPayReturn
    // =========================================================================

    @Test
    void handleVNPayReturn_TC_UT_PAY_013_shouldMarkOrderCompletedAndCreateEnrollment() {
        // TC-UT-PAY-013
        // Arrange: callback thanh cong + order dang PENDING.
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

        // Act
        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        // Assert: redirect success, order chuyen COMPLETED, tao enrollment.
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
    void handleVNPayReturn_TC_UT_PAY_014_shouldNotCreateDuplicateEnrollmentForCompletedOrder() {
        // TC-UT-PAY-014
        // Arrange: order da COMPLETED va enrollment da ton tai.
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

        // Act
        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        // Assert: khong save lai order va khong tao enrollment trung.
        assertTrue(redirectView.getUrl().contains("/order-status?status=success"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    
        @Test
    void handleVNPayReturn_TC_UT_PAY_015_shouldRedirectFailWhenTxnRefIsInvalid() {
        // TC-UT-PAY-015
        when(request.getParameter("vnp_TxnRef")).thenReturn("invalid_txn");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertEquals("http://localhost:5173/order-status?status=fail", redirectView.getUrl());
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    
        @Test
    void handleVNPayReturn_TC_UT_PAY_016_shouldRedirectToFailWhenResponseCodeIsNotSuccess() {
        // TC-UT-PAY-016
        when(request.getParameter("vnp_TxnRef")).thenReturn(null);
        when(request.getParameter("vnp_ResponseCode")).thenReturn("07");

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("/order-status?status=fail"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    
        @Test
    void handleVNPayReturn_TC_UT_PAY_017_shouldRedirectFailWhenOrderNotFoundForValidTxnRef() {
        // TC-UT-PAY-017
        when(request.getParameter("vnp_TxnRef")).thenReturn("20260504120000_9999");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(orderRepository.findById(9999L)).thenReturn(Optional.empty());

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("?status=fail"));
        verify(orderRepository, never()).save(any(Orders.class));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    
        @Test
    void handleVNPayReturn_TC_UT_PAY_018_shouldRedirectFailWhenOrderDetailNotFoundForValidTxnRef() {
        // TC-UT-PAY-018
        Orders pendingOrder = Orders.builder()
                .id(802L)
                .user(student)
                .status(EStatusOrder.PENDING)
                .build();

        when(request.getParameter("vnp_TxnRef")).thenReturn("20260504120000_802");
        when(request.getParameter("vnp_ResponseCode")).thenReturn("00");
        when(request.getParameter("vnp_TransactionNo")).thenReturn("999888");
        when(request.getParameter("vnp_Amount")).thenReturn("49900000");
        when(request.getParameter("vnp_PayDate")).thenReturn("20260504120500");
        when(orderRepository.findById(802L)).thenReturn(Optional.of(pendingOrder));
        when(orderDetailRepository.findByOrderId(802L)).thenReturn(Optional.empty());

        RedirectView redirectView = paymentService.handleVNPayReturn(request);

        assertTrue(redirectView.getUrl().contains("?status=fail"));
        verify(enrollmentRepository, never()).save(any(com.doan2025.webtoeic.domain.Enrollment.class));
    }

    @Test
    void handleVNPayReturn_TC_UT_PAY_019_specSaysCancelledOrderCannotBeCompletedByCallback() {
        // TC-UT-PAY-019 (SPEC MISMATCH - FAIL)
        // Spec STP_PAY_023: success callback should only complete PENDING orders.
        // Code checks != COMPLETED (not == PENDING), so CANCELLED order gets set to COMPLETED.
        // THIS TEST WILL FAIL intentionally.
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

        // Spec: CANCELLED order must not be updated; save should never be called.
        // Actual: code sets status=COMPLETED and calls save -> test FAILS.
        verify(orderRepository, never()).save(any(Orders.class));
    }

}
