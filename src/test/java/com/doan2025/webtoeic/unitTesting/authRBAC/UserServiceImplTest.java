package com.doan2025.webtoeic.service.impl;

import com.doan2025.webtoeic.constants.Constants;
import com.doan2025.webtoeic.constants.enums.ERole;
import com.doan2025.webtoeic.constants.enums.ResponseCode;
import com.doan2025.webtoeic.constants.enums.ResponseObject;
import com.doan2025.webtoeic.domain.Student;
import com.doan2025.webtoeic.domain.User;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.StudentResponse;
import com.doan2025.webtoeic.dto.response.UserResponse;
import com.doan2025.webtoeic.exception.WebToeicException;
import com.doan2025.webtoeic.repository.UserRepository;
import com.doan2025.webtoeic.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserServiceImpl RBAC and account management flows.
 * Each test contains the matching Test Case ID required by the report.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserServiceImpl userService;

    private User managerUser;
    private User consultantUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setEmail("manager@test.com");
        managerUser.setRole(ERole.MANAGER);
        managerUser.setPassword("encodedManagerPassword");
        managerUser.setIsActive(true);
        managerUser.setIsDelete(false);

        consultantUser = new User();
        consultantUser.setId(2L);
        consultantUser.setEmail("consultant@test.com");
        consultantUser.setRole(ERole.CONSULTANT);
        consultantUser.setPassword("encodedConsultantPassword");
        consultantUser.setIsActive(true);
        consultantUser.setIsDelete(false);

        Student student = new Student();
        student.setEducation("Old education");
        student.setMajor("Old major");

        studentUser = new User();
        studentUser.setId(3L);
        studentUser.setEmail("student@test.com");
        studentUser.setRole(ERole.STUDENT);
        studentUser.setPassword("encodedStudentPassword");
        studentUser.setIsActive(true);
        studentUser.setIsDelete(false);
        studentUser.setFirstName("Old");
        studentUser.setLastName("Name");
        studentUser.setStudent(student);
    }

    // TC_RBAC_001
    @Test
    @DisplayName("TC_RBAC_001: getListUserFilter uses manager scope and clears empty filter roles")
    void getListUserFilterUsesManagerScopeAndClearsEmptyFilterRoles() {
        // Why this case exists:
        // RBAC is not only about @PreAuthorize on controllers. This service also applies data-scope RBAC,
        // meaning manager can query a broader role set than lower roles.
        SearchBaseDto dto = new SearchBaseDto();
        dto.setUserRoles(List.of());
        UserResponse expectedUser = new UserResponse();
        expectedUser.setRole(ERole.TEACHER.name());

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(userRepository.findListUserFilter(eq(dto), eq(Constants.ROLE_BELOW_MANAGER), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(expectedUser)));

        Page<UserResponse> result = userService.getListUserFilter(request, dto, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertNull(dto.getUserRoles());
        verify(userRepository, times(1))
                .findListUserFilter(eq(dto), eq(Constants.ROLE_BELOW_MANAGER), any(Pageable.class));
    }

    // TC_RBAC_002
    @Test
    @DisplayName("TC_RBAC_002: getListUserFilter uses consultant scope for non-manager users")
    void getListUserFilterUsesConsultantScopeForNonManagerUsers() {
        // Why this case exists:
        // This is the complementary scope test to the manager case.
        // It proves lower roles receive a narrower repository filter.
        SearchBaseDto dto = new SearchBaseDto();

        when(jwtUtil.getEmailFromToken(request)).thenReturn("consultant@test.com");
        when(userRepository.findByEmail("consultant@test.com")).thenReturn(Optional.of(consultantUser));
        when(userRepository.findListUserFilter(eq(dto), eq(Constants.ROLE_BELOW_CONSULTANT), any(Pageable.class)))
                .thenReturn(Page.empty());

        userService.getListUserFilter(request, dto, Pageable.unpaged());

        verify(userRepository, times(1))
                .findListUserFilter(eq(dto), eq(Constants.ROLE_BELOW_CONSULTANT), any(Pageable.class));
    }

    // TC_RBAC_003
    @Test
    @DisplayName("TC_RBAC_003: getListUserFilter throws when token email cannot be resolved")
    void getListUserFilterThrowsWhenTokenEmailCannotBeResolved() {
        when(jwtUtil.getEmailFromToken(request)).thenReturn(null);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.getListUserFilter(request, new SearchBaseDto(), Pageable.unpaged())
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
        verify(userRepository, never()).findByEmail(any());
    }

    // TC_RBAC_004
    @Test
    @DisplayName("TC_RBAC_004: getUserCurrent returns current user profile")
    void getUserCurrentReturnsCurrentUserProfile() {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setRole(ERole.MANAGER.name());

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findUser("manager@test.com")).thenReturn(Optional.of(userResponse));

        UserResponse result = userService.getUserCurrent(request);

        assertEquals(1L, result.getId());
        assertEquals(ERole.MANAGER.name(), result.getRole());
    }

    // TC_RBAC_005
    @Test
    @DisplayName("TC_RBAC_005: getUserDetails throws when id is null")
    void getUserDetailsThrowsWhenIdIsNull() {
        UserRequest userRequest = new UserRequest();

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.getUserDetails(userRequest)
        );

        assertEquals(ResponseCode.IS_NULL, exception.getResponseCode());
        assertEquals(ResponseObject.ID, exception.getResponseObject());
    }

    // TC_RBAC_006
    @Test
    @DisplayName("TC_RBAC_006: updateUserDetails changes password when old password matches")
    void updateUserDetailsChangesPasswordWhenOldPasswordMatches() {
        UserRequest userRequest = new UserRequest();
        userRequest.setOldPassword("oldPassword");
        userRequest.setPassword("newPassword");

        UserResponse mappedResponse = new UserResponse();
        mappedResponse.setId(1L);

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(passwordEncoder.matches("oldPassword", "encodedManagerPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(managerUser)).thenReturn(managerUser);
        when(modelMapper.map(managerUser, UserResponse.class)).thenReturn(mappedResponse);

        UserResponse result = userService.updateUserDetails(request, userRequest);

        assertEquals(1L, result.getId());
        assertEquals("encodedNewPassword", managerUser.getPassword());
        verify(userRepository, times(1)).save(managerUser);
    }

    // TC_RBAC_007
    @Test
    @DisplayName("TC_RBAC_007: updateUserDetails rejects password change when old password is wrong")
    void updateUserDetailsRejectsPasswordChangeWhenOldPasswordIsWrong() {
        UserRequest userRequest = new UserRequest();
        userRequest.setOldPassword("wrongOldPassword");
        userRequest.setPassword("newPassword");

        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findByEmail("manager@test.com")).thenReturn(Optional.of(managerUser));
        when(passwordEncoder.matches("wrongOldPassword", "encodedManagerPassword")).thenReturn(false);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.updateUserDetails(request, userRequest)
        );

        assertEquals(ResponseCode.NOT_MATCHED, exception.getResponseCode());
        assertEquals(ResponseObject.PASSWORD, exception.getResponseObject());
        verify(userRepository, never()).save(any(User.class));
    }

    // TC_RBAC_008
    @Test
    @DisplayName("TC_RBAC_008: updateUserDetails updates student profile fields")
    void updateUserDetailsUpdatesStudentProfileFields() {
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName("New");
        userRequest.setLastName("Student");
        userRequest.setEducation("Bachelor");
        userRequest.setMajor("English");

        UserResponse mappedUserResponse = new UserResponse();
        StudentResponse mappedStudentResponse = StudentResponse.builder()
                .education("Bachelor")
                .major("English")
                .build();

        when(jwtUtil.getEmailFromToken(request)).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(userRepository.save(studentUser)).thenReturn(studentUser);
        when(modelMapper.map(studentUser, UserResponse.class)).thenReturn(mappedUserResponse);
        when(modelMapper.map(studentUser.getStudent(), StudentResponse.class)).thenReturn(mappedStudentResponse);

        UserResponse result = userService.updateUserDetails(request, userRequest);

        assertEquals("New", studentUser.getFirstName());
        assertEquals("Student", studentUser.getLastName());
        assertEquals("Bachelor", studentUser.getStudent().getEducation());
        assertEquals("English", studentUser.getStudent().getMajor());
        assertEquals("Bachelor", result.getStudent().getEducation());
        assertEquals("English", result.getStudent().getMajor());
    }

    // TC_RBAC_009
    @Test
    @DisplayName("TC_RBAC_009: deleteOrDisableUser updates flags and persists the user")
    void deleteOrDisableUserUpdatesFlagsAndPersistsTheUser() {
        UserRequest userRequest = new UserRequest();
        userRequest.setId(3L);
        userRequest.setIsActive(false);
        userRequest.setIsDelete(true);
        UserResponse mappedResponse = new UserResponse();
        mappedResponse.setIsActive(false);
        mappedResponse.setIsDelete(true);

        when(userRepository.findById(3L)).thenReturn(Optional.of(studentUser));
        when(userRepository.save(studentUser)).thenReturn(studentUser);
        when(modelMapper.map(studentUser, UserResponse.class)).thenReturn(mappedResponse);

        UserResponse result = userService.deleteOrDisableUser(userRequest);

        assertEquals(false, studentUser.getIsActive());
        assertEquals(true, studentUser.getIsDelete());
        assertEquals(false, result.getIsActive());
        assertEquals(true, result.getIsDelete());
    }

    // TC_RBAC_010
    @Test
    @DisplayName("TC_RBAC_010: resetPassword throws when reset token is missing")
    void resetPasswordThrowsWhenResetTokenIsMissing() {
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("newPassword");

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.resetPassword(userRequest)
        );

        assertEquals(ResponseCode.IS_NULL, exception.getResponseCode());
        assertEquals(ResponseObject.TOKEN, exception.getResponseObject());
    }

    // TC_RBAC_011
    @Test
    @DisplayName("TC_RBAC_011: resetPassword throws when token email is not found")
    void resetPasswordThrowsWhenTokenEmailIsNotFound() {
        UserRequest userRequest = new UserRequest();
        userRequest.setToken("reset-token");
        userRequest.setPassword("newPassword");

        when(jwtUtil.getEmailFromTokenString("reset-token")).thenReturn(null);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.resetPassword(userRequest)
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.EMAIL, exception.getResponseObject());
        verify(userRepository, never()).save(any(User.class));
    }

    // TC_RBAC_012
    @Test
    @DisplayName("TC_RBAC_012: resetPassword encodes and saves the new password for valid token")
    void resetPasswordEncodesAndSavesTheNewPasswordForValidToken() {
        UserRequest userRequest = new UserRequest();
        userRequest.setToken("reset-token");
        userRequest.setPassword("newPassword");

        when(jwtUtil.getEmailFromTokenString("reset-token")).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewStudentPassword");

        userService.resetPassword(userRequest);

        assertEquals("encodedNewStudentPassword", studentUser.getPassword());
        verify(userRepository, times(1)).save(studentUser);
    }

    // TC_RBAC_013
    @Test
    @DisplayName("TC_RBAC_013: resetPassword rejects inactive deleted users")
    void resetPasswordRejectsInactiveDeletedUsers() {
        UserRequest userRequest = new UserRequest();
        userRequest.setToken("reset-token");
        userRequest.setPassword("newPassword");

        studentUser.setIsActive(false);
        studentUser.setIsDelete(true);

        when(jwtUtil.getEmailFromTokenString("reset-token")).thenReturn("student@test.com");
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(studentUser));

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.resetPassword(userRequest)
        );

        assertEquals(ResponseCode.NOT_AVAILABLE, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
        verify(userRepository, never()).save(any(User.class));
    }

    // TC_RBAC_014
    @Test
    @DisplayName("TC_RBAC_014: getUserCurrent throws when current user projection is missing")
    void getUserCurrentThrowsWhenCurrentUserProjectionIsMissing() {
        // Why this case exists:
        // A valid token is not enough if the current user projection cannot be loaded.
        // This covers the repository-empty branch after token resolution.
        when(jwtUtil.getEmailFromToken(request)).thenReturn("manager@test.com");
        when(userRepository.findUser("manager@test.com")).thenReturn(Optional.empty());

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.getUserCurrent(request)
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
    }

    // TC_RBAC_015
    @Test
    @DisplayName("TC_RBAC_015: getUserDetails throws when user id does not exist")
    void getUserDetailsThrowsWhenUserIdDoesNotExist() {
        // Why this case exists:
        // Positive detail lookup alone is not sufficient. We also need to prove the service
        // returns the correct business exception when the requested target user is absent.
        UserRequest userRequest = new UserRequest();
        userRequest.setId(999L);

        when(userRepository.findUserById(userRequest)).thenReturn(Optional.empty());

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.getUserDetails(userRequest)
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
    }

    // TC_RBAC_016
    @Test
    @DisplayName("TC_RBAC_016: updateUserDetails throws when token email cannot be resolved")
    void updateUserDetailsThrowsWhenTokenEmailCannotBeResolved() {
        // Why this case exists:
        // Self-update depends on the current authenticated user. If token parsing fails,
        // the service must stop immediately instead of updating an unknown account.
        when(jwtUtil.getEmailFromToken(request)).thenReturn(null);

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.updateUserDetails(request, new UserRequest())
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
        verify(userRepository, never()).findByEmail(any());
    }

    // TC_RBAC_017
    @Test
    @DisplayName("TC_RBAC_017: deleteOrDisableUser throws when user id does not exist")
    void deleteOrDisableUserThrowsWhenUserIdDoesNotExist() {
        // Why this case exists:
        // Administrative actions need negative-path coverage too.
        // This proves delete/disable does not silently succeed on missing users.
        UserRequest userRequest = new UserRequest();
        userRequest.setId(404L);

        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        WebToeicException exception = assertThrows(
                WebToeicException.class,
                () -> userService.deleteOrDisableUser(userRequest)
        );

        assertEquals(ResponseCode.NOT_EXISTED, exception.getResponseCode());
        assertEquals(ResponseObject.USER, exception.getResponseObject());
        verify(userRepository, never()).save(any(User.class));
    }
}
