package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.config.SecurityConfig;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.dto.request.AuthenticationRequest;
import com.doan2025.webtoeic.dto.request.RegisterRequest;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.request.VerifyRequest;
import com.doan2025.webtoeic.dto.response.AuthenticationResponse;
import com.doan2025.webtoeic.dto.response.VerifyResponse;
import com.doan2025.webtoeic.exception.GlobalExceptionHandler;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.security.AuthenticationService;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomerJwtDecoder customerJwtDecoder;

    @Test
    @DisplayName("TC_API_AUTH_001: POST /api/v1/auth/login is public and returns authentication payload")
    void loginIsPublicAndReturnsAuthenticationPayload() throws Exception {
        // Why this case exists:
        // The login endpoint is listed as public in SecurityConfig, so users must be able to call it
        // without an existing token. This test proves the API layer returns the service result in the
        // expected ApiResponse wrapper instead of being blocked by security.
        AuthenticationRequest request = new AuthenticationRequest("student@test.com", "password123");
        AuthenticationResponse response = AuthenticationResponse.builder()
                .authenticated(true)
                .token("jwt-token")
                .role(4)
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(ResponseCode.SUCCESS.getMessage(ResponseObject.LOGIN)))
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.role").value(4));
    }

    @Test
    @DisplayName("TC_API_AUTH_002: POST /api/v1/auth/login returns mapped error when authentication fails")
    void loginReturnsMappedErrorWhenAuthenticationFails() throws Exception {
        // Why this case exists:
        // Besides the success path, the controller must also map business exceptions into a stable
        // HTTP response. If the teacher asks, this test demonstrates integration between controller
        // and GlobalExceptionHandler, not only service logic.
        AuthenticationRequest request = new AuthenticationRequest("student@test.com", "wrongPassword");

        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new WebToeicException(ResponseCode.INVALID_PASSWORD, ResponseObject.PASSWORD));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(ResponseCode.INVALID_PASSWORD.getMessage(ResponseObject.PASSWORD)));
    }

    @Test
    @DisplayName("TC_API_AUTH_003: POST /api/v1/auth/register is public and delegates to service")
    void registerIsPublicAndDelegatesToService() throws Exception {
        // Why this case exists:
        // Register is another public endpoint. The important point is to verify the API contract:
        // request JSON reaches the controller, the service is called once, and the response message
        // follows the common ApiResponse format.
        RegisterRequest request = new RegisterRequest("new@test.com", "password123", "New", "User", null);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value(ResponseCode.SUCCESS.getMessage(ResponseObject.REGISTER)));

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("TC_API_AUTH_004: POST /api/v1/auth/verify-email is public and delegates to service")
    void verifyEmailIsPublicAndDelegatesToService() throws Exception {
        // Why this case exists:
        // Forgot-password entrypoints are often forgotten when testing auth. This proves that the
        // endpoint is reachable without authentication and that the controller hands work to the service.
        VerifyRequest request = VerifyRequest.builder().email("student@test.com").build();

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseCode.SUCCESS.getMessage(ResponseObject.VERIFY)));

        verify(authenticationService).verifyMail(any(VerifyRequest.class));
    }

    @Test
    @DisplayName("TC_API_AUTH_005: POST /api/v1/auth/verify-otp is public and returns reset token")
    void verifyOtpIsPublicAndReturnsResetToken() throws Exception {
        // Why this case exists:
        // This checks the second step of forgot-password flow at API level. The output is not just
        // status 200; the controller must return the token inside ApiResponse.data for the next step.
        VerifyRequest request = VerifyRequest.builder().email("student@test.com").otp(123456).build();
        VerifyResponse response = VerifyResponse.builder().token("reset-token").build();

        when(authenticationService.verify_otp(any(VerifyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("reset-token"));
    }

    @Test
    @DisplayName("TC_API_AUTH_006: POST /api/v1/auth/reset-password is public and delegates to user service")
    void resetPasswordIsPublicAndDelegatesToUserService() throws Exception {
        // Why this case exists:
        // Reset-password is intentionally public because the reset token itself is the credential.
        // This test shows that the endpoint does not require an existing login session.
        UserRequest request = new UserRequest();
        request.setToken("reset-token");
        request.setPassword("newPassword");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseCode.SUCCESS.getMessage(ResponseObject.USER)));

        verify(userService).resetPassword(any(UserRequest.class));
    }

    @Test
    @DisplayName("TC_API_AUTH_007: GET /api/v1/auth/refresh rejects unauthenticated requests")
    void refreshRejectsUnauthenticatedRequests() throws Exception {
        // Why this case exists:
        // Refresh is the opposite of login/register: it must be protected. This verifies that the
        // request is stopped by Spring Security before reaching the service when no authentication exists.
        mockMvc.perform(get("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(ResponseCode.UNAUTHENTICATED.getMessage()));

        verify(authenticationService, never()).refreshToken(any());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_API_AUTH_008: GET /api/v1/auth/refresh accepts authenticated requests")
    void refreshAcceptsAuthenticatedRequests() throws Exception {
        // Why this case exists:
        // After proving unauthenticated access is blocked, we also need the positive branch:
        // a logged-in user should be able to refresh successfully and receive a new token payload.
        AuthenticationResponse response = AuthenticationResponse.builder()
                .authenticated(true)
                .token("new-token")
                .role(4)
                .build();

        when(authenticationService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("new-token"));
    }

    @Test
    @DisplayName("TC_API_AUTH_009: GET /api/v1/auth/logout rejects unauthenticated requests")
    void logoutRejectsUnauthenticatedRequests() throws Exception {
        // Why this case exists:
        // Logout must also be protected. This is useful when explaining that auth testing should
        // include both public endpoints and protected endpoints, not only login business logic.
        mockMvc.perform(get("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ResponseCode.UNAUTHENTICATED.getMessage()));

        verify(authenticationService, never()).logout(any());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_API_AUTH_010: GET /api/v1/auth/logout accepts authenticated requests")
    void logoutAcceptsAuthenticatedRequests() throws Exception {
        // Why this case exists:
        // This completes the logout API pair: one test for unauthorized access, one for the valid path.
        // Together they prove the endpoint is protected but still usable by authenticated users.
        mockMvc.perform(get("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ResponseCode.SUCCESS.getMessage(ResponseObject.LOGOUT)));

        verify(authenticationService).logout(any());
    }
}
