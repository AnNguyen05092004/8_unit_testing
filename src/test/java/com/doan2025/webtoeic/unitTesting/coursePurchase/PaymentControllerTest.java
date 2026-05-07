package com.doan2025.webtoeic.unitTesting.coursePurchase;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.controller.PaymentController;
import com.doan2025.webtoeic.dto.response.PaymentResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.RedirectView;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcTest cho PaymentController.
 * Kiem tra HTTP request/response layer - khong goi truc tiep controller method.
 *
 * PaymentController co 2 endpoint:
 *   - GET /create?orderId=X  (yeu cau STUDENT auth, tra ApiResponse)
 *   - GET /return            (public - trong PUBLIC_ENDPOINTS_GET cua SecurityConfig, tra RedirectView)
 *
 * Luu y ky thuat:
 *   - @TestConfiguration @EnableMethodSecurity: bat @PreAuthorize hoat dong trong slice test
 *   - /return la public endpoint (khong can auth)
 *   - PAY-CTRL-005: HAS_PAID co HttpStatus.OK nen tra HTTP 200, phan biet qua message
 *
 * Bao phu:
 *   - createVNPayPayment: success, no-auth (401), wrong-role (403), missing-param (400),
 *                         HAS_PAID (order da thanh toan), NOT_EXISTED, NOT_PERMISSION
 *   - handleVNPayReturn: redirect success (public), redirect fail (public)
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    /**
     * Kich hoat @EnableMethodSecurity trong WebMvcTest context.
     * Bat @PreAuthorize("hasRole('STUDENT')") hoat dong dung.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private CustomerJwtDecoder customerJwtDecoder;

    // =========================================================================
    // GET /api/v1/payment/create
    // =========================================================================

    /**
     * PAY-CTRL-001
     * GET /create?orderId=1 - STUDENT hop le - tra ve 200 + PaymentResponse co URL thanh toan.
     * Kiem tra: HTTP 200, code=200, data.status="success", data.URL khong rong.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_withValidOrder_returns200AndPaymentUrl() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .status("success")
                .message("Payment URL generated")
                .URL("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=1000000")
                .build();
        // Dung any() thay vi eq(1L) de tranh boxing issue voi Long argument matcher
        when(paymentService.createVNPayPayment(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("success"))
                // Jackson serialize field 'URL' thanh 'url' (lowercase) do Lombok getter getUrl()
                .andExpect(jsonPath("$.data.url").isNotEmpty());
    }

    /**
     * PAY-CTRL-002
     * GET /create?orderId=1 - khong co auth - tra ve 401 Unauthenticated.
     * Chi kiem tra HTTP status 401.
     */
    @Test
    void createPayment_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * PAY-CTRL-003
     * GET /create?orderId=1 - role TEACHER sai quyen - tra ve 403 Forbidden.
     * @PreAuthorize("hasRole('STUDENT')") that bai -> AccessDeniedException
     * -> GlobalExceptionHandler tra 403, code=404, message="You do not have permission".
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void createPayment_withWrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * PAY-CTRL-004
     * GET /create - thieu param orderId bat buoc - 400 Bad Request.
     * MissingServletRequestParameterException -> GlobalExceptionHandler -> 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_withMissingOrderId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/payment/create"))
                .andExpect(status().isBadRequest());
    }

    /**
     * PAY-CTRL-005
     * GET /create?orderId=2 - don hang da duoc thanh toan (COMPLETED).
     * Service nem WebToeicException(HAS_PAID, ORDER).
     * Luu y thiet ke API: HAS_PAID co HttpStatus.OK nen tra HTTP 200.
     * Phan biet voi success that su bang message ("has been paid" thay vi payment URL).
     * Kiem tra: HTTP 200, code=200, message chua thong tin "has been paid".
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenOrderAlreadyPaid_returns200WithHasPaidMessage() throws Exception {
        doThrow(new WebToeicException(ResponseCode.HAS_PAID, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(2L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.HAS_PAID.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
     * PAY-CTRL-006
     * GET /create?orderId=999 - don hang khong ton tai.
     * Service nem WebToeicException(NOT_EXISTED, ORDER) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenOrderNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(999L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
         * PAY-CTRL-007
         * GET /create?orderId=800 - user khong phai chu don hang.
         * THUC TE: PaymentServiceImpl.createVNPayPayment() kiem tra user.getEmail().equals(order.getUser().getEmail())
         * neu sai -> throw WebToeicException(NOT_PERMISSION, ResponseObject.USER)
         * -> HTTP 403, message = "User not permission " (khong phai "Order not permission ")
     */
    @Test
        @WithMockUser(roles = "STUDENT")
        void createPayment_whenNotOrderOwner_returns403WithNotPermission() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER))
                .when(paymentService).createVNPayPayment(eq(800L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "800"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_PERMISSION.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    // =========================================================================
    // GET /api/v1/payment/return  (public - khong can auth)
    // =========================================================================

    /**
     * PAY-CTRL-015
     * GET /return voi callback thanh cong tu VNPay (vnp_ResponseCode=00).
     * Endpoint public (trong PUBLIC_ENDPOINTS_GET cua SecurityConfig), khong yeu cau auth.
     * Service tra RedirectView voi URL success -> controller tra 302 redirect.
     * Kiem tra: HTTP 3xx, Location header tro den URL success.
     * Luu y: @WithMockUser duoc them de dam bao request di qua security filter chain
     * trong WebMvcTest context (PUBLIC_ENDPOINTS_GET co the khong duoc apply chinh xac).
     */
    @Test
    @WithMockUser
    void handleVNPayReturn_withSuccessResponseCode_redirectsToSuccessUrl() throws Exception {
        String successUrl = "http://localhost:5173/order-status?status=success";
        when(paymentService.handleVNPayReturn(any()))
                .thenReturn(new RedirectView(successUrl));

        mockMvc.perform(get("/api/v1/payment/return")
                        .param("vnp_TxnRef", "20260505120000_801")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TransactionStatus", "00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(successUrl));
    }

    /**
     * PAY-CTRL-016
     * GET /return voi callback that bai tu VNPay (vnp_ResponseCode=07).
     * Endpoint public, khong yeu cau auth.
     * Service tra RedirectView voi URL fail -> controller tra 302 redirect.
     * Kiem tra: HTTP 3xx, URL redirect chua "status=fail".
     * Luu y: @WithMockUser duoc them vi @WebMvcTest context co the require auth.
     */
    @Test
    @WithMockUser
    void handleVNPayReturn_withFailureResponseCode_redirectsToFailUrl() throws Exception {
        String failUrl = "http://localhost:5173/order-status?status=fail";
        when(paymentService.handleVNPayReturn(any()))
                .thenReturn(new RedirectView(failUrl));

        mockMvc.perform(get("/api/v1/payment/return")
                        .param("vnp_TxnRef", "20260505120000_801")
                        .param("vnp_ResponseCode", "07"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(failUrl));
    }

    /**
     * PAY-CTRL-008
     * GET /create?orderId=300 - so tien don hang am (totalAmount < 0).
     * THUC TE: PaymentServiceImpl.createVNPayPayment() kiem tra amount < 0
     * -> throw WebToeicException(INVALID, AMOUNT) -> HTTP 400.
     * Case: don hang co gia tri am do loi du lieu.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenNegativeAmount_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.INVALID, ResponseObject.AMOUNT))
                .when(paymentService).createVNPayPayment(eq(300L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "300"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.INVALID.getMessage()
                                .replace("{entity}", ResponseObject.AMOUNT.toString())));
    }

    /**
     * PAY-CTRL-009
     * GET /create?orderId=901 - user trong token khong ton tai.
     * Service nem WebToeicException(NOT_EXISTED, USER) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenUserNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))
                .when(paymentService).createVNPayPayment(eq(901L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "901"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    /**
     * PAY-CTRL-010
     * GET /create?orderId=902 - order detail khong ton tai.
     * Service nem WebToeicException(NOT_EXISTED, ORDER) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenOrderDetailNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(902L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "902"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
     * PAY-CTRL-011
     * GET /create?orderId=903 - he thong tao chu ky VNPay loi.
     * Service nem WebToeicException(NOT_SUCCESS, PAYMENT) -> GlobalExceptionHandler -> 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_whenGatewaySigningFails_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_SUCCESS, ResponseObject.PAYMENT))
                .when(paymentService).createVNPayPayment(eq(903L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "903"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_SUCCESS.getMessage()
                                .replace("{entity}", ResponseObject.PAYMENT.toString())));
    }

    // =========================================================================
    // Additional HIGH PRIORITY edge cases
    // =========================================================================

    /**
     * PAY-CTRL-012
     * GET /create?orderId=0 - zero/invalid ID - Spring validation.
     * MethodArgumentTypeMismatchException hoac ConstraintViolationException
     * neu Long id co validation @Positive, @NotNull.
     * Neu khong co validation, service se nhan orderId=0 va throw NOT_EXISTED(ORDER).
     * Test: nhận orderId=0 -> should be 400 hoac 404 tuy validation.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_withZeroOrderId_returns400OrNot() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(0L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * PAY-CTRL-017
     * GET /return - khong co vnp_ResponseCode param.
     * VNPay callback can dung certain params; neu thieu -> service should handle gracefully.
     * Test: missing vnp_ResponseCode -> service should still process (perhaps as fail case).
     * Endpoint public, khong can auth.
     */
    @Test
    @WithMockUser
    void handleVNPayReturn_withMissingResponseCode_stillProcesses() throws Exception {
        String failUrl = "http://localhost:5173/order-status?status=fail";
        when(paymentService.handleVNPayReturn(any()))
                .thenReturn(new RedirectView(failUrl));

        mockMvc.perform(get("/api/v1/payment/return")
                        .param("vnp_TxnRef", "20260505120000_801"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(failUrl));
    }

    /**
     * PAY-CTRL-013
     * GET /create?orderId=-1 - negative order ID.
     * PaymentServiceImpl.createVNPayPayment() kiem tra orderRepository.findById(-1) -> Optional.empty()
     * -> throw WebToeicException(NOT_EXISTED, ORDER) -> HTTP 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_withNegativeOrderId_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(-1L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
     * PAY-CTRL-014
     * GET /create?orderId=999999999999 - very large order ID.
     * Spring Long parse OK, service kiem tra orderRepository.findById(999999999999) -> Optional.empty()
     * -> throw WebToeicException(NOT_EXISTED, ORDER) -> HTTP 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createPayment_withVeryLargeOrderId_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.ORDER))
                .when(paymentService).createVNPayPayment(eq(999999999999L), any());

        mockMvc.perform(get("/api/v1/payment/create")
                        .param("orderId", "999999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }
}
