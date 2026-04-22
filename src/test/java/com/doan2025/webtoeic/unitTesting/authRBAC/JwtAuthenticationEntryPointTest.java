package com.doan2025.webtoeic.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationEntryPointTest {

    private final JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

    @Test
    @DisplayName("TC_API_AUTH_011: JwtAuthenticationEntryPoint returns standardized unauthenticated response")
    void commenceReturnsStandardizedUnauthenticatedResponse() throws Exception {
        // Why this case exists:
        // When Spring Security rejects a request before it reaches the controller,
        // this entry point is responsible for the response body. Testing it separately
        // proves the API still returns the project's standard JSON error format.
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("\"code\":404"));
        assertTrue(response.getContentAsString().contains("\"message\":\"Unauthenticated\""));
    }
}
