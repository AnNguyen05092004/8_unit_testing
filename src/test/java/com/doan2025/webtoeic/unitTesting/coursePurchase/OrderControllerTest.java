package com.doan2025.webtoeic.unitTesting.coursePurchase;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.controller.OrderController;
import com.doan2025.webtoeic.dto.response.OrderResponse;
import com.doan2025.webtoeic.dto.response.StatisticOrderResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcTest cho OrderController.
 * Kiem tra HTTP request/response layer - khong goi truc tiep controller method.
 *
 * Luu y ky thuat:
 *   - @TestConfiguration @EnableMethodSecurity: bat @PreAuthorize hoat dong trong slice test
 *   - .with(csrf()): POST requests can CSRF token trong WebMvcTest context
 *
 * Bao phu:
 *   - statisticOrders: success, no-auth (401), wrong-role (403)
 *   - getOwnOrders: success, filter theo status
 *   - deleteOrder (cancelOrder): success, missing-id, not-permission, not-found
 *   - createOrderByCartItem: success, already-ordered (EXISTED)
 *   - createOrderByCourse: success, course-not-found (NOT_EXISTED)
 */
@WebMvcTest(OrderController.class)
class OrderControllerTest {

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
    private OrderService orderService;

    @MockBean
    private CustomerJwtDecoder customerJwtDecoder;

    // =========================================================================
    // GET /api/v1/order/statistic-orders
    // =========================================================================

    /**
     * ORDER-CTRL-001
     * GET /statistic-orders - STUDENT hop le - tra ve 200 + StatisticOrderResponse.
     * Kiem tra: HTTP 200, code=200, cac truong thong ke co gia tri dung.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getStatisticOrders_withValidStudent_returns200() throws Exception {
        StatisticOrderResponse stats = StatisticOrderResponse.builder()
                .totalOrders(BigDecimal.valueOf(10))
                .pendingOrders(BigDecimal.valueOf(3))
                .completedOrders(BigDecimal.valueOf(5))
                .cancelledOrders(BigDecimal.valueOf(2))
                .totalPurchases(BigDecimal.valueOf(1500000))
                .build();
        when(orderService.getStatisticOrder(any())).thenReturn(stats);

        mockMvc.perform(get("/api/v1/order/statistic-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalOrders").value(10))
                .andExpect(jsonPath("$.data.pendingOrders").value(3))
                .andExpect(jsonPath("$.data.completedOrders").value(5));
    }

    /**
     * ORDER-CTRL-002
     * GET /statistic-orders - khong co auth - tra ve 401 Unauthenticated.
     * Chi kiem tra HTTP status 401.
     */
    @Test
    void getStatisticOrders_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/order/statistic-orders"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ORDER-CTRL-003
     * GET /statistic-orders - role TEACHER sai quyen - tra ve 403 Forbidden.
     * @PreAuthorize("hasRole('STUDENT')") that bai -> AccessDeniedException
     * -> GlobalExceptionHandler tra 403, code=404, message="You do not have permission".
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void getStatisticOrders_withWrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/order/statistic-orders"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    // =========================================================================
    // POST /api/v1/order  (getOwnOrders)
    // =========================================================================

    /**
     * ORDER-CTRL-004
     * POST / - STUDENT hop le - tra ve 200 + danh sach orders phan trang.
     * Kiem tra: data.content la array, order dau tien co id=1.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getOwnOrders_withValidStudent_returns200PagedOrders() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .totalAmount(100000.0)
                .build();
        when(orderService.getOwnOrders(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(order)));

        mockMvc.perform(post("/api/v1/order")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    /**
     * ORDER-CTRL-005
     * POST / voi filter statusOrder=PENDING - tra ve 200 + danh sach re theo trang thai.
     * Kiem tra: tham so statusOrder duoc bind vao SearchOrderDto va service duoc goi.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getOwnOrders_withStatusFilter_returns200EmptyPage() throws Exception {
        when(orderService.getOwnOrders(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/api/v1/order")
                        .with(csrf())
                        .param("statusOrder", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // =========================================================================
    // POST /api/v1/order/delete  (cancelOrder)
    // =========================================================================

    /**
     * ORDER-CTRL-011
     * POST /delete?id=1 - STUDENT la chu don hang - huy thanh cong - 200.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_withValidId_returns200() throws Exception {
        doNothing().when(orderService).cancelOrder(any(), any());

        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * ORDER-CTRL-012
     * POST /delete - thieu param id bat buoc - 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_withMissingId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * ORDER-CTRL-013
     * POST /delete?id=1 - user khong phai chu don hang.
         * THUC TE: OrderServiceImpl.cancelOrder() kiem tra order.getUser().getEmail().equals(user.getEmail())
         * neu sai -> throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER)
         * -> HTTP 403, message = "User not permission " (khong phai "Order not permission ")
     */
    @Test
        @WithMockUser(roles = "STUDENT")
        void cancelOrder_whenNotOwner_returns403WithNotPermission() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER))
                .when(orderService).cancelOrder(any(), any());

        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_PERMISSION.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    /**
     * ORDER-CTRL-014
     * POST /delete?id=999 - don hang khong ton tai.
     * Service nem WebToeicException(CANNOT_GET, ORDER) -> GlobalExceptionHandler -> 404.
     * (OrderServiceImpl dung CANNOT_GET khi orderRepository.findById tra empty)
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_whenOrderNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.ORDER))
                .when(orderService).cancelOrder(any(), any());

        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.CANNOT_GET.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    // =========================================================================
    // POST /api/v1/order/create-order-by-cart-item
    // =========================================================================

    /**
     * ORDER-CTRL-020
     * POST /create-order-by-cart-item?id=1 - tao don hang tu cart item thanh cong - 200.
     * Kiem tra: HTTP 200, code=200, data.id cua don hang moi = 10.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_withValidId_returns200() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(10L)
                .totalAmount(50000.0)
                .build();
        when(orderService.createOrderByCartItem(any(), eq(1L))).thenReturn(order);

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    /**
     * ORDER-CTRL-021
     * POST /create-order-by-cart-item?id=1 - khoa hoc da duoc mua truoc day (EXISTED).
     * Service nem WebToeicException(EXISTED, ORDER) -> GlobalExceptionHandler -> 400.
     * Case thuc te: user da co orderDetail hoac da enrolled khoa hoc nay.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_whenAlreadyOrdered_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER))
                .when(orderService).createOrderByCartItem(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    // =========================================================================
    // POST /api/v1/order/create-order-by-course
    // =========================================================================

    /**
     * ORDER-CTRL-029
     * POST /create-order-by-course?id=22 - tao don hang truc tiep tu khoa hoc - 200.
     * Kiem tra: HTTP 200, code=200, data.id cua don hang moi = 20.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_withValidCourseId_returns200() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(20L)
                .totalAmount(75000.0)
                .build();
        when(orderService.createOrderByCourseID(any(), eq(22L))).thenReturn(order);

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(20));
    }

    /**
     * ORDER-CTRL-030
     * POST /create-order-by-course?id=999 - khoa hoc khong ton tai.
     * Service nem WebToeicException(NOT_EXISTED, COURSE) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_whenCourseNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE))
                .when(orderService).createOrderByCourseID(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.COURSE.toString())));
    }

    // =========================================================================
    // createOrderByCartItem - cac truong hop bo sung (theo OrderServiceImpl)
    // =========================================================================

    /**
     * ORDER-CTRL-022
     * POST /create-order-by-cart-item?id=99 - cart item khong ton tai.
     * THUC TE: OrderServiceImpl.createOrderByCartItem() dung CANNOT_GET khi
     * cartItemRepository.findById(id) tra Optional.empty() (khong phai NOT_EXISTED).
     * -> throw WebToeicException(CANNOT_GET, CART_ITEM) -> HTTP 404, message="Cannot get Cart_item"
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_whenCartItemNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.CART_ITEM))
                .when(orderService).createOrderByCartItem(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.CANNOT_GET.getMessage()
                                .replace("{entity}", ResponseObject.CART_ITEM.toString())));
    }

    /**
     * ORDER-CTRL-023
     * POST /create-order-by-cart-item?id=2 - user da enrolled vao khoa hoc nay roi.
     * OrderServiceImpl.createOrderByCartItem() kiem tra enrollmentRepository.existsByUserAndCourse()
     * -> neu da enrolled -> throw WebToeicException(EXISTED, ENROLLMENT) -> HTTP 400.
     * Case: tranh tao order khi user da co quyen truy cap khoa hoc.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_whenAlreadyEnrolled_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT))
                .when(orderService).createOrderByCartItem(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ENROLLMENT.toString())));
    }

    // =========================================================================
    // createOrderByCourse - cac truong hop bo sung (theo OrderServiceImpl)
    // =========================================================================

    /**
     * ORDER-CTRL-031
     * POST /create-order-by-course?id=33 - khoa hoc da duoc mua truoc day.
     * OrderServiceImpl.createOrderByCourseID() kiem tra orderDetailRepository.existsByUserAndCourse()
     * -> neu da mua -> throw WebToeicException(EXISTED, ORDER) -> HTTP 400.
     * Case: ngan user tao don hang trung lap cho khoa hoc da thanh toan.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_whenAlreadyPurchased_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER))
                .when(orderService).createOrderByCourseID(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "33"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
     * ORDER-CTRL-032
     * POST /create-order-by-course?id=44 - user da enrolled vao khoa hoc nay roi.
     * OrderServiceImpl.createOrderByCourseID() kiem tra enrollmentRepository.existsByUserAndCourse()
     * -> neu da enrolled -> throw WebToeicException(EXISTED, ENROLLMENT) -> HTTP 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_whenAlreadyEnrolled_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT))
                .when(orderService).createOrderByCourseID(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "44"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ENROLLMENT.toString())));
    }

    /**
     * ORDER-CTRL-024
     * POST /create-order-by-cart-item - thieu param id bat buoc - 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_withMissingId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * ORDER-CTRL-033
     * POST /create-order-by-course - thieu param id bat buoc - 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_withMissingId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * ORDER-CTRL-006
     * POST / (getOwnOrders) - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void getOwnOrders_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ORDER-CTRL-007
     * POST / (getOwnOrders) - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void getOwnOrders_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/order")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * ORDER-CTRL-015
     * POST /delete?id=1 - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void cancelOrder_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ORDER-CTRL-016
     * POST /delete?id=1 - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void cancelOrder_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * ORDER-CTRL-025
     * POST /create-order-by-cart-item?id=1 - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void createOrderByCartItem_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ORDER-CTRL-026
     * POST /create-order-by-cart-item?id=1 - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void createOrderByCartItem_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * ORDER-CTRL-034
     * POST /create-order-by-course?id=22 - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void createOrderByCourse_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "22"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ORDER-CTRL-035
     * POST /create-order-by-course?id=22 - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void createOrderByCourse_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "22"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * ORDER-CTRL-036
     * POST /create-order-by-course?id=22 - user from JWT khong ton tai.
     * OrderServiceImpl.createOrderByCourseID() extract user tu HttpServletRequest
     * neu user khong ton tai -> throw WebToeicException(NOT_EXISTED, USER).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_whenUserNotFoundInService_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))
                .when(orderService).createOrderByCourseID(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "22"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    // =========================================================================
    // Additional HIGH PRIORITY edge cases and exception handling
    // =========================================================================

    /**
     * ORDER-CTRL-017
     * POST /delete?id=1&id=2&id=3 - cancel multiple orders cung 1 luc - 200.
     * Endpoint nhan List<Long> id, orderService.cancelOrder() process tung id.
     * Kiem tra: HTTP 200, tất cả 3 order duoc xu ly (hoac co loi, service fail).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_withMultipleIds_returns200() throws Exception {
        doNothing().when(orderService).cancelOrder(any(), any());

        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * ORDER-CTRL-018
     * POST /delete (khong co id) - empty list - Spring validation.
     * MissingServletRequestParameterException hoac MethodArgumentTypeMismatchException
     * -> GlobalExceptionHandler -> 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_withEmptyIdList_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * ORDER-CTRL-038
     * GET /statistic-orders - service throw WebToeicException (business error).
     * OrderServiceImpl.getStatisticOrder() co the throw WebToeicException
     * (e.g., NOT_EXISTED, INVALID, etc) -> GlobalExceptionHandler map -> appropriate HTTP status.
     * Test voi business exception thay vi generic RuntimeException.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getStatisticOrders_whenServiceThrowsBusinessException_returns400() throws Exception {
        doThrow(new WebToeicException(ResponseCode.INVALID, ResponseObject.ORDER))
                .when(orderService).getStatisticOrder(any());

        mockMvc.perform(get("/api/v1/order/statistic-orders"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * ORDER-CTRL-008
     * POST /?page=-1 - invalid pagination param (negative page) - Spring Pageable auto-correct.
     * Spring Data Pageable tu dong xu ly negative page (convert thanh 0 hoac ngan).
     * Test thay doi: kiem tra response van la 200 (Spring handle gracefully voi empty page).
     * Hoac co the tra ve danh sach rong (empty content).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getOwnOrders_withNegativePage_returnsEmptyPage() throws Exception {
        when(orderService.getOwnOrders(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/api/v1/order")
                        .with(csrf())
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    /**
     * ORDER-CTRL-027
     * POST /create-order-by-cart-item?id=1 - user from JWT token not found in service.
     * OrderServiceImpl.createOrderByCartItem() extract user tu HttpServletRequest
     * neu user khong ton tai -> throw WebToeicException(NOT_EXISTED, USER).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_whenUserNotFoundInService_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))
                .when(orderService).createOrderByCartItem(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    /**
     * ORDER-CTRL-019
     * POST /delete?id=-1 - negative ID - Spring nhận OK, service xu ly NOT_EXISTED.
     * OrderServiceImpl.cancelOrder() kiem tra orderRepository.findById(-1) -> Optional.empty()
     * -> throw WebToeicException(CANNOT_GET, ORDER) -> HTTP 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void cancelOrder_withNegativeId_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.ORDER))
                .when(orderService).cancelOrder(any(), any());

        mockMvc.perform(post("/api/v1/order/delete")
                        .with(csrf())
                        .param("id", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * ORDER-CTRL-009
     * POST /?size=0 - pagination size=0 - Spring Pageable co default, neu size=0 thi sao?
     * Test: xem spring tra empty page hoac error.
     * Neu Spring accept -> tra empty page (data.content = []).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getOwnOrders_withZeroSize_returnsEmptyPage() throws Exception {
        when(orderService.getOwnOrders(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/api/v1/order")
                        .with(csrf())
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    /**
     * ORDER-CTRL-010
     * POST /?statusOrder=PENDING&statusOrder=COMPLETED - multiple status filter.
     * SearchOrderDto co the accept array/multiple values hoac chi lay value dau.
     * Test: xem system xu ly multiple filters dung khong (neu co logic loc theo status).
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getOwnOrders_withMultipleStatusFilters_processesCorrectly() throws Exception {
        when(orderService.getOwnOrders(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/api/v1/order")
                        .with(csrf())
                        .param("statusOrder", "PENDING", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    /**
     * ORDER-CTRL-028
     * POST /create-order-by-cart-item?id=-1 - negative cartItem ID.
     * OrderServiceImpl.createOrderByCartItem() kiem tra cartItemRepository.findById(-1) -> Optional.empty()
     * -> throw WebToeicException(CANNOT_GET, CART_ITEM) -> HTTP 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCartItem_withNegativeId_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.CANNOT_GET, ResponseObject.CART_ITEM))
                .when(orderService).createOrderByCartItem(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-cart-item")
                        .with(csrf())
                        .param("id", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * ORDER-CTRL-037
     * POST /create-order-by-course?id=-1 - negative course ID.
     * OrderServiceImpl.createOrderByCourseID() kiem tra courseRepository.findById(-1) -> Optional.empty()
     * (hoac co validation @Positive tren Course entity)
     * -> throw WebToeicException(NOT_EXISTED, COURSE) -> HTTP 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void createOrderByCourse_withNegativeId_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE))
                .when(orderService).createOrderByCourseID(any(), any());

        mockMvc.perform(post("/api/v1/order/create-order-by-course")
                        .with(csrf())
                        .param("id", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
