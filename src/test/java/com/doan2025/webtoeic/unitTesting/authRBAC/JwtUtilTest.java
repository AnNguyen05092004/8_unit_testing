package com.doan2025.webtoeic.utils;

import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.exception.WebToeicException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JwtUtil helper methods.
 * Each test contains the matching Test Case ID required by the report.
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final String RAW_SIGNER_KEY =
            "jwtUtilitySignerKeyThatIsLongEnoughForHmacVerification123456789012345";
    private static final String TEST_EMAIL = "test@example.com";

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "SIGNER_KEY", RAW_SIGNER_KEY);
    }

    // TC_AUTH_101
    @Test
    @DisplayName("TC_AUTH_101: getJwtFromRequest extracts the bearer token")
    void getJwtFromRequestExtractsTheBearerToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");

        String token = jwtUtil.getJwtFromRequest(request);

        assertEquals("valid.jwt.token", token);
    }

    // TC_AUTH_102
    @Test
    @DisplayName("TC_AUTH_102: getJwtFromRequest throws when Authorization header is missing")
    void getJwtFromRequestThrowsWhenAuthorizationHeaderIsMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> jwtUtil.getJwtFromRequest(request)
        );

        assertEquals(ResponseCode.CANNOT_GET, exception.getResponseCode());
        assertEquals(ResponseObject.TOKEN, exception.getResponseObject());
    }

    // TC_AUTH_103
    @Test
    @DisplayName("TC_AUTH_103: getEmailFromToken returns the JWT subject")
    void getEmailFromTokenReturnsTheJwtSubject() {
        String token = buildToken(TEST_EMAIL, RAW_SIGNER_KEY);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        String email = jwtUtil.getEmailFromToken(request);

        assertEquals(TEST_EMAIL, email);
    }

    // TC_AUTH_104
    @Test
    @DisplayName("TC_AUTH_104: getEmailFromToken throws IllegalArgumentException for invalid JWT")
    void getEmailFromTokenThrowsIllegalArgumentExceptionForInvalidJwt() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.jwt.token");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jwtUtil.getEmailFromToken(request)
        );

        assertTrue(exception.getMessage().contains("Invalid JWT token"));
    }

    // TC_AUTH_105
    @Test
    @DisplayName("TC_AUTH_105: getEmailFromTokenString returns the JWT subject")
    void getEmailFromTokenStringReturnsTheJwtSubject() {
        String token = buildToken(TEST_EMAIL, RAW_SIGNER_KEY);

        String email = jwtUtil.getEmailFromTokenString(token);

        assertEquals(TEST_EMAIL, email);
    }

    // TC_AUTH_106
    @Test
    @DisplayName("TC_AUTH_106: getSigningKey decodes the base64 configured signer key")
    void getSigningKeyDecodesTheBase64ConfiguredSignerKey() {
        String base64SignerKey = Base64.getEncoder().encodeToString(RAW_SIGNER_KEY.getBytes());
        ReflectionTestUtils.setField(jwtUtil, "SIGNER_KEY", base64SignerKey);

        SecretKey signingKey = jwtUtil.getSigningKey();

        assertNotNull(signingKey);
        assertEquals("HmacSHA512", signingKey.getAlgorithm());
    }

    // TC_AUTH_107
    @Test
    @DisplayName("TC_AUTH_107: getSigningKey throws when signer key is not configured")
    void getSigningKeyThrowsWhenSignerKeyIsNotConfigured() {
        ReflectionTestUtils.setField(jwtUtil, "SIGNER_KEY", null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> jwtUtil.getSigningKey());

        assertTrue(exception.getMessage().contains("signerKey is null"));
    }

    private String buildToken(String subject, String signerKey) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600_000))
                .signWith(Keys.hmacShaKeyFor(signerKey.getBytes()))
                .compact();
    }
}
