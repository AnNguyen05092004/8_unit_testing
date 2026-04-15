package com.doan2025.webtoeic;

import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Consultant;
import com.doan2025.webtoeic.domain.ForgotPassword;
import com.doan2025.webtoeic.domain.InvalidatedToken;
import com.doan2025.webtoeic.domain.Manager;
import com.doan2025.webtoeic.domain.Student;
import com.doan2025.webtoeic.domain.Teacher;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.request.AuthenticationRequest;
import com.doan2025.webtoeic.dto.request.IntrospectRequest;
import com.doan2025.webtoeic.dto.request.RegisterRequest;
import com.doan2025.webtoeic.dto.request.VerifyRequest;
import com.doan2025.webtoeic.dto.response.AuthenticationResponse;
import com.doan2025.webtoeic.dto.response.IntrospectResponse;
import com.doan2025.webtoeic.dto.response.VerifyResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.ConsultantRepository;
import com.doan2025.webtoeic.repository.ForgotPasswordRepository;
import com.doan2025.webtoeic.repository.InvalidatedTokenRepository;
import com.doan2025.webtoeic.repository.ManagerRepository;
import com.doan2025.webtoeic.repository.StudentRepository;
import com.doan2025.webtoeic.repository.TeacherRepository;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.security.AuthenticationService;
import com.doan2025.webtoeic.service.EmailService;
import com.doan2025.webtoeic.utils.JwtUtil;
import com.nimbusds.jose.JOSEException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthenticationService.
 * Each test contains the matching Test Case ID required by the report.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String TEST_SIGNER_KEY =
            "testSecretKeyForJwtSigningThatIsLongEnoughForHS512Algorithm1234567890";
    private static final String ALT_SIGNER_KEY =
            "alternateSecretKeyForJwtSigningThatIsAlsoLongEnoughForHS512Algo1234567";
    private static final long TEST_VALID_DURATION = 3600L;
    private static final long TEST_REFRESHABLE_DURATION = 86400L;
    private static final String TEST_ISSUER = "webtoeic-test";

    @Mock
    private UserRepository userRepository;
    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private ForgotPasswordRepository forgotPasswordRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User activeStudent;
    private User inactiveDeletedUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", TEST_SIGNER_KEY);
        ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", TEST_VALID_DURATION);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", TEST_REFRESHABLE_DURATION);
        ReflectionTestUtils.setField(authenticationService, "ISSUER", TEST_ISSUER);

        activeStudent = new User();
        activeStudent.setId(1L);
        activeStudent.setEmail("student@test.com");
        activeStudent.setPassword("encodedPassword");
        activeStudent.setRole(ERole.STUDENT);
        activeStudent.setIsActive(true);
        activeStudent.setIsDelete(false);
        activeStudent.setFirstName("John");
        activeStudent.setLastName("Student");

        inactiveDeletedUser = new User();
        inactiveDeletedUser.setId(2L);
        inactiveDeletedUser.setEmail("deleted@test.com");
        inactiveDeletedUser.setPassword("encodedPassword");
        inactiveDeletedUser.setRole(ERole.STUDENT);
        inactiveDeletedUser.setIsActive(false);
        inactiveDeletedUser.setIsDelete(true);
        inactiveDeletedUser.setFirstName("Deleted");
        inactiveDeletedUser.setLastName("User");
    }

    // TC_AUTH_001
    @Test
    @DisplayName("TC_AUTH_001: authenticate returns token for valid credentials")
    void authenticateReturnsTokenForValidCredentials() {
        AuthenticationRequest request = new AuthenticationRequest(" student@test.com ", "password123");

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response.getToken());
        assertTrue(response.isAuthenticated());
        assertEquals(ERole.STUDENT.getValue(), response.getRole());
    }

    // TC_AUTH_002
    @Test
    @DisplayName("TC_AUTH_002: authenticate throws when email does not exist")
    void authenticateThrowsWhenEmailDoesNotExist() {
        AuthenticationRequest request = new AuthenticationRequest("missing@test.com", "password123");

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // TC_AUTH_003
    @Test
    @DisplayName("TC_AUTH_003: authenticate throws when password is incorrect")
    void authenticateThrowsWhenPasswordIsIncorrect() {
        AuthenticationRequest request = new AuthenticationRequest("student@test.com", "wrongPassword");

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ResponseCode.INVALID_PASSWORD, exception.getResponseCode());
        assertEquals(ResponseObject.PASSWORD, exception.getResponseObject());
    }

    // TC_AUTH_004
    @Test
    @DisplayName("TC_AUTH_004: authenticate throws when account is unavailable")
    void authenticateThrowsWhenAccountIsUnavailable() {
        AuthenticationRequest request = new AuthenticationRequest("deleted@test.com", "password123");

        when(userRepository.findByEmail("deleted@test.com")).thenReturn(Optional.of(inactiveDeletedUser));

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.authenticate(request)
        );

        assertEquals(ResponseCode.NOT_AVAILABLE, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
    }

    // TC_AUTH_005
    @Test
    @DisplayName("TC_AUTH_005: register creates a student account when role is omitted")
    void registerCreatesStudentAccountWhenRoleIsOmitted() {
        RegisterRequest request = new RegisterRequest(
                "newstudent@test.com",
                "password123",
                "John",
                "Doe",
                null
        );

        when(userRepository.existsByEmail("newstudent@test.com")).thenReturn(false);
        when(userRepository.existsByCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });

        authenticationService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
        verify(studentRepository, times(1)).save(any(Student.class));
        verify(managerRepository, never()).save(any(Manager.class));
        verify(consultantRepository, never()).save(any(Consultant.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    // TC_AUTH_006
    @Test
    @DisplayName("TC_AUTH_006: register throws when email already exists")
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "existing@test.com",
                "password123",
                "Jane",
                "Doe",
                null
        );

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.register(request)
        );

        assertEquals(ResponseCode.EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
        verify(userRepository, never()).save(any(User.class));
    }

    // TC_AUTH_007
    @Test
    @DisplayName("TC_AUTH_007: register throws when role value is invalid")
    void registerThrowsWhenRoleValueIsInvalid() {
        RegisterRequest request = new RegisterRequest(
                "newuser@test.com",
                "password123",
                "Test",
                "User",
                99
        );

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.register(request)
        );

        assertEquals(ResponseCode.INVALID, exception.getResponseCode());
        assertEquals(ResponseObject.ROLE, exception.getResponseObject());
    }

    // TC_AUTH_008
    @Test
    @DisplayName("TC_AUTH_008: register creates a manager account for manager role")
    void registerCreatesManagerAccountForManagerRole() {
        RegisterRequest request = new RegisterRequest(
                "manager@test.com",
                "password123",
                "Bob",
                "Manager",
                ERole.MANAGER.getValue()
        );

        when(userRepository.existsByEmail("manager@test.com")).thenReturn(false);
        when(userRepository.existsByCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.register(request);

        verify(managerRepository, times(1)).save(any(Manager.class));
        verify(consultantRepository, never()).save(any(Consultant.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    // TC_AUTH_009
    @Test
    @DisplayName("TC_AUTH_009: register creates a consultant account for consultant role")
    void registerCreatesConsultantAccountForConsultantRole() {
        RegisterRequest request = new RegisterRequest(
                "consultant@test.com",
                "password123",
                "Cora",
                "Consultant",
                ERole.CONSULTANT.getValue()
        );

        when(userRepository.existsByEmail("consultant@test.com")).thenReturn(false);
        when(userRepository.existsByCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.register(request);

        verify(consultantRepository, times(1)).save(any(Consultant.class));
        verify(managerRepository, never()).save(any(Manager.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    // TC_AUTH_010
    @Test
    @DisplayName("TC_AUTH_010: introspect returns valid true for a signed active token")
    void introspectReturnsTrueForSignedActiveToken() throws JOSEException, ParseException {
        String validToken = buildToken(activeStudent, TEST_SIGNER_KEY, Instant.now(), Instant.now().plusSeconds(600));

        when(invalidatedTokenRepository.existsByToken(anyString())).thenReturn(false);

        IntrospectResponse response = authenticationService.introspect(
                IntrospectRequest.builder().token(validToken).build()
        );

        assertTrue(response.isValid());
    }

    // TC_AUTH_011
    @Test
    @DisplayName("TC_AUTH_011: introspect returns valid false for a token with invalid signature")
    void introspectReturnsFalseForTokenWithInvalidSignature() throws JOSEException, ParseException {
        String invalidSignatureToken = buildToken(
                activeStudent,
                ALT_SIGNER_KEY,
                Instant.now(),
                Instant.now().plusSeconds(600)
        );

        IntrospectResponse response = authenticationService.introspect(
                IntrospectRequest.builder().token(invalidSignatureToken).build()
        );

        assertFalse(response.isValid());
    }

    // TC_AUTH_012
    @Test
    @DisplayName("TC_AUTH_012: logout stores the token identifier when token is refreshable")
    void logoutStoresTheTokenIdentifierWhenTokenIsRefreshable() throws ParseException, JOSEException {
        String validToken = buildToken(activeStudent, TEST_SIGNER_KEY, Instant.now(), Instant.now().plusSeconds(600));

        when(jwtUtil.getJwtFromRequest(httpServletRequest)).thenReturn(validToken);
        when(invalidatedTokenRepository.existsByToken(anyString())).thenReturn(false);

        authenticationService.logout(httpServletRequest);

        ArgumentCaptor<InvalidatedToken> invalidatedTokenCaptor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository, times(1)).save(invalidatedTokenCaptor.capture());
        assertNotNull(invalidatedTokenCaptor.getValue().getToken());
        assertNotNull(invalidatedTokenCaptor.getValue().getExpiryTime());
    }

    // TC_AUTH_013
    @Test
    @DisplayName("TC_AUTH_013: logout ignores expired refresh token without saving blacklist entry")
    void logoutIgnoresExpiredRefreshTokenWithoutSavingBlacklistEntry() throws ParseException, JOSEException {
        String expiredRefreshToken = buildToken(
                activeStudent,
                TEST_SIGNER_KEY,
                Instant.now().minus(2, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS)
        );

        when(jwtUtil.getJwtFromRequest(httpServletRequest)).thenReturn(expiredRefreshToken);

        assertDoesNotThrow(() -> authenticationService.logout(httpServletRequest));

        verify(invalidatedTokenRepository, never()).save(any(InvalidatedToken.class));
    }

    // TC_AUTH_014
    @Test
    @DisplayName("TC_AUTH_014: refreshToken invalidates old token and returns a new token")
    void refreshTokenInvalidatesOldTokenAndReturnsNewToken() throws ParseException, JOSEException {
        String validToken = buildToken(activeStudent, TEST_SIGNER_KEY, Instant.now(), Instant.now().plusSeconds(600));

        when(jwtUtil.getJwtFromRequest(httpServletRequest)).thenReturn(validToken);
        when(invalidatedTokenRepository.existsByToken(anyString())).thenReturn(false);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));

        AuthenticationResponse response = authenticationService.refreshToken(httpServletRequest);

        assertTrue(response.isAuthenticated());
        assertNotNull(response.getToken());
        assertEquals(ERole.STUDENT.getValue(), response.getRole());
        verify(invalidatedTokenRepository, times(1)).save(any(InvalidatedToken.class));
    }

    // TC_AUTH_015
    @Test
    @DisplayName("TC_AUTH_015: refreshToken throws when token owner no longer exists")
    void refreshTokenThrowsWhenTokenOwnerNoLongerExists() throws ParseException, JOSEException {
        String validToken = buildToken(activeStudent, TEST_SIGNER_KEY, Instant.now(), Instant.now().plusSeconds(600));

        when(jwtUtil.getJwtFromRequest(httpServletRequest)).thenReturn(validToken);
        when(invalidatedTokenRepository.existsByToken(anyString())).thenReturn(false);
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.empty());

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.refreshToken(httpServletRequest)
        );

        assertEquals(ResponseCode.UNAUTHENTICATED, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
    }

    // TC_AUTH_016
    @Test
    @DisplayName("TC_AUTH_016: verifyMail replaces old OTP and sends a new email")
    void verifyMailReplacesOldOtpAndSendsNewEmail() {
        VerifyRequest request = VerifyRequest.builder().email("student@test.com").build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));
        when(forgotPasswordRepository.existsByUser(activeStudent)).thenReturn(true);

        authenticationService.verifyMail(request);

        verify(forgotPasswordRepository, times(1)).deleteByUser(activeStudent);
        verify(forgotPasswordRepository, times(1)).save(any(ForgotPassword.class));
        verify(emailService, times(1)).sendEmail(eq("student@test.com"), anyString(), anyString());
    }

    // TC_AUTH_017
    @Test
    @DisplayName("TC_AUTH_017: verifyMail throws when email is missing")
    void verifyMailThrowsWhenEmailIsMissing() {
        VerifyRequest request = VerifyRequest.builder().email(null).build();

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.verifyMail(request)
        );

        assertEquals(ResponseCode.IS_NULL, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
    }

    // TC_AUTH_018
    @Test
    @DisplayName("TC_AUTH_018: verifyOtp returns reset token and removes OTP record")
    void verifyOtpReturnsResetTokenAndRemovesOtpRecord() {
        VerifyRequest request = VerifyRequest.builder()
                .email("student@test.com")
                .otp(123456)
                .build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .id(1L)
                .otp(123456)
                .user(activeStudent)
                .expiryTime(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));
        when(forgotPasswordRepository.findByOtpAndUser(123456, activeStudent)).thenReturn(Optional.of(forgotPassword));

        VerifyResponse response = authenticationService.verify_otp(request);

        assertNotNull(response.getToken());
        verify(forgotPasswordRepository, times(1)).deleteById(1L);
    }

    // TC_AUTH_019
    @Test
    @DisplayName("TC_AUTH_019: verifyOtp throws when OTP is missing")
    void verifyOtpThrowsWhenOtpIsMissing() {
        VerifyRequest request = VerifyRequest.builder()
                .email("student@test.com")
                .otp(null)
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.verify_otp(request)
        );

        assertEquals(ResponseCode.IS_NULL, exception.getResponseCode());
        assertEquals(ResponseObject.CODE, exception.getResponseObject());
    }

    // TC_AUTH_020
    @Test
    @DisplayName("TC_AUTH_020: verifyOtp deletes expired OTP and throws token expired")
    void verifyOtpDeletesExpiredOtpAndThrowsTokenExpired() {
        VerifyRequest request = VerifyRequest.builder()
                .email("student@test.com")
                .otp(123456)
                .build();
        ForgotPassword expiredForgotPassword = ForgotPassword.builder()
                .id(2L)
                .otp(123456)
                .user(activeStudent)
                .expiryTime(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
                .build();

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(activeStudent));
        when(forgotPasswordRepository.findByOtpAndUser(123456, activeStudent))
                .thenReturn(Optional.of(expiredForgotPassword));

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> authenticationService.verify_otp(request)
        );

        assertEquals(ResponseCode.TOKEN_EXPIRED, exception.getResponseCode());
        assertEquals(ResponseObject.CODE, exception.getResponseObject());
        verify(forgotPasswordRepository, times(1)).deleteById(2L);
    }

    private String buildToken(User user, String signerKey, Instant issuedAt, Instant expirationTime) {
        SecretKey key = Keys.hmacShaKeyFor(signerKey.getBytes());
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuer(TEST_ISSUER)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expirationTime))
                .setId(UUID.randomUUID().toString())
                .claim("scope", "ROLE_" + user.getRole().getCode())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
