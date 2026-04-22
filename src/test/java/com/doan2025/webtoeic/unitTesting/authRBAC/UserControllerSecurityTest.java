package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.config.SecurityConfig;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.UserRequest;
import com.doan2025.webtoeic.dto.response.StudentResponse;
import com.doan2025.webtoeic.dto.response.UserResponse;
import com.doan2025.webtoeic.exception.GlobalExceptionHandler;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomerJwtDecoder customerJwtDecoder;

    @Test
    @DisplayName("TC_API_RBAC_001: GET /api/v1/user rejects unauthenticated requests")
    void getCurrentUserRejectsUnauthenticatedRequests() throws Exception {
        // Why this case exists:
        // RBAC starts with authentication. Before checking roles, the API must reject anonymous users.
        // This proves the endpoint is protected at filter-chain level.
        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(404));

        verify(userService, never()).getUserCurrent(any());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_API_RBAC_002: GET /api/v1/user allows student role")
    void getCurrentUserAllowsStudentRole() throws Exception {
        // Why this case exists:
        // The annotation on this endpoint explicitly allows STUDENT. This test proves that the
        // configured role expression matches the intended business rule.
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setRole("STUDENT");
        userResponse.setStudent(StudentResponse.builder().education("Bachelor").build());

        when(userService.getUserCurrent(any())).thenReturn(userResponse);

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.student.education").value("Bachelor"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_API_RBAC_003: POST /api/v1/user/filter forbids student role")
    void filterUsersForbidsStudentRole() throws Exception {
        // Why this case exists:
        // A good RBAC suite must contain denial cases, not only allow cases.
        // Students are not in the @PreAuthorize expression for this endpoint, so the expected result is 403.
        SearchBaseDto request = new SearchBaseDto();

        mockMvc.perform(post("/api/v1/user/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(404));

        verify(userService, never()).getListUserFilter(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "CONSULTANT")
    @DisplayName("TC_API_RBAC_004: POST /api/v1/user/filter allows consultant role")
    void filterUsersAllowsConsultantRole() throws Exception {
        // Why this case exists:
        // This is the positive pair for the previous test. Together, the two tests explain that
        // the RBAC rule is enforced correctly for both forbidden and allowed roles.
        SearchBaseDto request = new SearchBaseDto();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(2L);
        userResponse.setRole("STUDENT");

        when(userService.getListUserFilter(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(userResponse)));

        mockMvc.perform(post("/api/v1/user/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].role").value("STUDENT"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TC_API_RBAC_005: POST /api/v1/user allows teacher role")
    void getUserDetailsAllowsTeacherRole() throws Exception {
        // Why this case exists:
        // User-detail lookup is allowed for TEACHER, CONSULTANT, and MANAGER. Testing TEACHER here
        // avoids only proving one admin role and shows the policy is not manager-only.
        UserRequest request = new UserRequest();
        request.setId(3L);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(3L);
        userResponse.setRole("STUDENT");

        when(userService.getUserDetails(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_API_RBAC_006: POST /api/v1/user forbids student role")
    void getUserDetailsForbidsStudentRole() throws Exception {
        // Why this case exists:
        // This endpoint is intentionally more restricted than GET /api/v1/user.
        // The test helps explain the difference between "view my own profile" and "query another user detail".
        UserRequest request = new UserRequest();
        request.setId(3L);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserDetails(any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("TC_API_RBAC_007: POST /api/v1/user/update-own-info allows manager role")
    void updateOwnInfoAllowsManagerRole() throws Exception {
        // Why this case exists:
        // Self-update is open to all authenticated business roles. I kept MANAGER here to show this
        // endpoint is role-inclusive and not limited to students.
        UserRequest request = new UserRequest();
        request.setFirstName("Updated");

        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName("Updated");
        userResponse.setRole("MANAGER");

        when(userService.updateUserDetails(any(), any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/user/update-own-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    @WithMockUser(roles = "CONSULTANT")
    @DisplayName("TC_API_RBAC_008: POST /api/v1/user/delete-user allows consultant role")
    void deleteUserAllowsConsultantRole() throws Exception {
        // Why this case exists:
        // Destructive endpoints deserve explicit RBAC tests. Consultant is allowed here, so this test
        // proves the rule for a non-manager administrative role.
        UserRequest request = new UserRequest();
        request.setId(10L);
        request.setIsDelete(true);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(10L);
        userResponse.setIsDelete(true);

        when(userService.deleteOrDisableUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/user/delete-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDelete").value(true));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TC_API_RBAC_009: POST /api/v1/user/delete-user forbids teacher role")
    void deleteUserForbidsTeacherRole() throws Exception {
        // Why this case exists:
        // TEACHER can view some user data but cannot delete users. This is a good example to explain
        // that RBAC is endpoint-specific and a role may be allowed in one API but denied in another.
        UserRequest request = new UserRequest();
        request.setId(10L);
        request.setIsDelete(true);

        mockMvc.perform(post("/api/v1/user/delete-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteOrDisableUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("TC_API_RBAC_010: POST /api/v1/user/disable-user allows manager role")
    void disableUserAllowsManagerRole() throws Exception {
        // Why this case exists:
        // Disable-user is another administrative action. Having both delete-user and disable-user tests
        // makes the RBAC suite more complete and reduces the chance of missing endpoint-level restrictions.
        UserRequest request = new UserRequest();
        request.setId(11L);
        request.setIsActive(false);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(11L);
        userResponse.setIsActive(false);

        when(userService.deleteOrDisableUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/user/disable-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }
}
