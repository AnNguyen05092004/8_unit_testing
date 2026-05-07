package com.doan2025.webtoeic.unitTesting.coursePurchase;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.controller.CartController;
import com.doan2025.webtoeic.dto.response.CartItemResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.service.CartItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * WebMvcTest cho CartController.
 * Kiem tra HTTP request/response layer - khong goi truc tiep controller method.
 * Simulate HTTP request that qua MockMvc, mock CartItemService bang @MockBean.
 *
 * Luu y ky thuat:
 *   - @TestConfiguration @EnableMethodSecurity: kich hoat @PreAuthorize trong slice test
 *   - .with(csrf()): bo sung CSRF token cho POST/DELETE (spring-security-test tu dong inject)
 *   - 401 test: chi kiem tra HTTP status, khong kiem tra JSON body vi JwtAuthenticationEntryPoint
 *     co the khong duoc kich hoat trong @WebMvcTest context (phu thuoc vao cau hinh oauth2RS)
 *
 * Bao phu:
 *   - Success (200): getCart, addToCart, removeFromCart
 *   - Unauthenticated (401): getCart khong co token
 *   - Forbidden (403): getCart voi role sai
 *   - Business exception: EXISTED, NOT_EXISTED, NOT_PERMISSION -> correct HTTP status + JSON
 *   - Edge case: thieu request param bat buoc -> 400
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    /**
     * Kich hoat @EnableMethodSecurity trong WebMvcTest context.
     * Khong co annotation nay, @PreAuthorize bi bo qua trong slice test
     * va tat ca user co the truy cap endpoint bat ke role.
     */
    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired
    private MockMvc mockMvc;

    /** Mock CartItemService - dependency chinh cua CartController */
    @MockBean
    private CartItemService cartItemService;

    /**
     * Phai mock CustomerJwtDecoder vi SecurityConfig inject no qua constructor.
     * Khong mock se gay NoSuchBeanDefinitionException khi khoi tao SecurityConfig.
     */
    @MockBean
    private CustomerJwtDecoder customerJwtDecoder;

    // =========================================================================
    // GET /api/v1/cart
    // =========================================================================

    /**
     * CART-CTRL-001
     * GET /api/v1/cart - STUDENT hop le - tra ve 200 + danh sach cart items.
     * Kiem tra: HTTP 200, JSON code=200, data la array, item dau tien co id dung.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getCart_withValidStudent_returns200AndCartItems() throws Exception {
        CartItemResponse item = new CartItemResponse();
        item.setId(1L);
        when(cartItemService.getInCart(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    /**
     * CART-CTRL-002
     * GET /api/v1/cart - khong co auth - tra ve 401 Unauthenticated.
     * Chi kiem tra HTTP status 401 (JSON body phu thuoc vao cau hinh authenticationEntryPoint).
     */
    @Test
    void getCart_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * CART-CTRL-003
     * GET /api/v1/cart - role TEACHER sai quyen - tra ve 403 Forbidden.
     * @PreAuthorize("hasRole('STUDENT')") that bai -> AccessDeniedException
     * -> GlobalExceptionHandler.handlingAccessDeniedException tra 403.
     * Yeu cau: @TestConfiguration @EnableMethodSecurity active.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void getCart_withWrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    // =========================================================================
    // POST /api/v1/cart/add-to-cart
    // =========================================================================

    /**
     * CART-CTRL-005
     * POST /add-to-cart?id=1 - them khoa hoc vao gio hang thanh cong - 200.
     * Them .with(csrf()) vi CSRF co the active trong WebMvcTest context voi Spring Security.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_withValidCourseId_returns200() throws Exception {
        doNothing().when(cartItemService).addToCart(any(), eq(1L));

        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.CREATE_SUCCESS.getMessage()
                                .replace("{entity}", ResponseObject.CART_ITEM.toString())));
    }

    /**
     * CART-CTRL-006
     * POST /add-to-cart - thieu param id bat buoc - 400 Bad Request.
     * MissingServletRequestParameterException -> GlobalExceptionHandler -> 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_withMissingId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * CART-CTRL-007
     * POST /add-to-cart?id=1 - khoa hoc da co trong gio hang (EXISTED).
     * Service nem WebToeicException(EXISTED, CART_ITEM) -> GlobalExceptionHandler -> 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_whenCourseAlreadyInCart_returns400WithExistedMessage() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.CART_ITEM))
                .when(cartItemService).addToCart(any(), eq(1L));

        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.CART_ITEM.toString())));
    }

    /**
     * CART-CTRL-008
     * POST /add-to-cart?id=99 - khoa hoc khong ton tai (NOT_EXISTED).
     * Service nem WebToeicException(NOT_EXISTED, COURSE) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_whenCourseNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.COURSE))
                .when(cartItemService).addToCart(any(), eq(99L));

        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.COURSE.toString())));
    }

    // =========================================================================
    // DELETE /api/v1/cart/remove-from-cart
    // =========================================================================

    /**
     * CART-CTRL-014
     * DELETE /remove-from-cart?id=1 - xoa cart item thanh cong - 200.
     * Them .with(csrf()) vi DELETE cung require CSRF token trong test context.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void removeFromCart_withValidId_returns200() throws Exception {
        doNothing().when(cartItemService).removeFromCart(any(), eq(1L));

        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.DELETE_SUCCESS.getMessage()
                                .replace("{entity}", ResponseObject.CART_ITEM.toString())));
    }

    /**
     * CART-CTRL-015
     * DELETE /remove-from-cart - thieu param id bat buoc - 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void removeFromCart_withMissingId_returns400() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /**
     * CART-CTRL-016
     * DELETE /remove-from-cart?id=1 - user khong phai chu so huu cart item.
         * THUC TE: CartItemServiceImpl.removeFromCart() nem NOT_PERMISSION voi ResponseObject.USER,
         * khong phai CART_ITEM. Vi service kiem tra `cartItem.getUser().getId().equals(user.getId())`.
         * Neu sai -> throw new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER)
         * -> HTTP 403, message = "User not permission "
     */
    @Test
        @WithMockUser(roles = "STUDENT")
        void removeFromCart_whenNotOwner_returns403WithNotPermission() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_PERMISSION, ResponseObject.USER))
                .when(cartItemService).removeFromCart(any(), eq(1L));

        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_PERMISSION.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    /**
     * CART-CTRL-017
     * DELETE /remove-from-cart?id=999 - cart item khong ton tai.
     * Service nem WebToeicException(NOT_EXISTED, CART_ITEM) -> GlobalExceptionHandler -> 404.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void removeFromCart_whenCartItemNotFound_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.CART_ITEM))
                .when(cartItemService).removeFromCart(any(), eq(999L));

        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.CART_ITEM.toString())));
    }

    // =========================================================================
    // addToCart - cac truong hop EXISTED bo sung (theo CartItemServiceImpl)
    // =========================================================================

    /**
     * CART-CTRL-009
     * POST /add-to-cart?id=5 - khoa hoc da duoc mua truoc day (da co orderDetail).
     * CartItemServiceImpl.addToCart() kiem tra orderDetailRepository.existsByUserAndCourse()
     * -> neu da mua -> throw WebToeicException(EXISTED, ORDER) -> HTTP 400.
     * Day la case quan trong de ngan user them vao gio hang khoa hoc da thanh toan.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_whenCourseAlreadyPurchased_returns400WithExistedOrder() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ORDER))
                .when(cartItemService).addToCart(any(), eq(5L));

        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ORDER.toString())));
    }

    /**
     * CART-CTRL-010
     * POST /add-to-cart?id=6 - user da enrolled vao khoa hoc nay roi.
     * CartItemServiceImpl.addToCart() kiem tra enrollmentRepository.existsByUserAndCourse()
     * -> neu da enrolled -> throw WebToeicException(EXISTED, ENROLLMENT) -> HTTP 400.
     * Case: user da duoc cap quyen truy cap khoa hoc bang cach khac (giao vien add, v.v.)
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_whenAlreadyEnrolled_returns400WithExistedEnrollment() throws Exception {
        doThrow(new WebToeicException(ResponseCode.EXISTED, ResponseObject.ENROLLMENT))
                .when(cartItemService).addToCart(any(), eq(6L));

        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.ENROLLMENT.toString())));
    }

    /**
     * CART-CTRL-011
     * POST /add-to-cart?id=1 - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void addToCart_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * CART-CTRL-012
     * POST /add-to-cart?id=1 - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void addToCart_withWrongRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * CART-CTRL-018
     * DELETE /remove-from-cart?id=1 - khong co auth - tra ve 401 Unauthenticated.
     */
    @Test
    void removeFromCart_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * CART-CTRL-019
     * DELETE /remove-from-cart?id=1 - role TEACHER sai quyen - tra ve 403 Forbidden.
     */
    @Test
    @WithMockUser(roles = "TEACHER")
    void removeFromCart_withWrongRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    /**
     * CART-CTRL-004
     * GET /api/v1/cart - cartItemService throw exception (e.g., user from token not found).
     * CartItemServiceImpl.getInCart() kiem tra user tu HttpServletRequest (JWT)
     * neu user khong ton tai -> throw WebToeicException(NOT_EXISTED, USER).
     * GlobalExceptionHandler tra 404 HTTP status.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void getCart_whenUserNotFoundInService_returns404() throws Exception {
        doThrow(new WebToeicException(ResponseCode.NOT_EXISTED, ResponseObject.USER))
                .when(cartItemService).getInCart(any());

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(
                        ResponseCode.NOT_EXISTED.getMessage()
                                .replace("{entity}", ResponseObject.USER.toString())));
    }

    /**
     * CART-CTRL-013
     * POST /add-to-cart?id=abc - non-numeric ID format - 400 Bad Request.
     * Spring MethodArgumentTypeMismatchException khi parse "abc" thanh Long
     * -> GlobalExceptionHandler -> 400 Bad Request.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void addToCart_withNonNumericId_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add-to-cart")
                        .with(csrf())
                        .param("id", "abc"))
                .andExpect(status().isBadRequest());
    }

    /**
     * CART-CTRL-020
     * DELETE /remove-from-cart?id=abc - non-numeric ID format - 400 Bad Request.
     * Spring MethodArgumentTypeMismatchException -> GlobalExceptionHandler -> 400.
     */
    @Test
    @WithMockUser(roles = "STUDENT")
    void removeFromCart_withNonNumericId_returns400() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/remove-from-cart")
                        .with(csrf())
                        .param("id", "abc"))
                .andExpect(status().isBadRequest());
    }
}
