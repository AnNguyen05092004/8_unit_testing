package com.doan2025.webtoeic.controller;

import com.doan2025.webtoeic.config.SecurityConfig;
import com.doan2025.webtoeic.dto.SearchBaseDto;
import com.doan2025.webtoeic.dto.request.LessonRequest;
import com.doan2025.webtoeic.dto.response.LessonResponse;
import com.doan2025.webtoeic.security.CustomerJwtDecoder;
import com.doan2025.webtoeic.security.JwtAuthenticationEntryPoint;
import com.doan2025.webtoeic.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonController.class)
@Import({SecurityConfig.class, JwtAuthenticationEntryPoint.class})
class LessonControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean LessonService lessonService;
    @MockBean CustomerJwtDecoder customerJwtDecoder;

    // TC_LESSON_API_001
    @Test
    @DisplayName("TC_LESSON_API_001: GET /api/v1/lesson returns 401 without auth (not in public GET list)")
    void getLessonDetailRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/lesson").param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    // TC_LESSON_API_002
    @Test
    @DisplayName("TC_LESSON_API_002: POST /api/v1/lesson/get-lessons returns 401 without auth (not in public POST list)")
    void getLessonsRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/v1/lesson/get-lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchBaseDto())))
                .andExpect(status().isUnauthorized());
    }

    // TC_LESSON_API_003
    @Test
    @DisplayName("TC_LESSON_API_003: POST /api/v1/lesson/create returns 401 without auth")
    void createLessonRequiresAuth() throws Exception {
        mockMvc.perform(post("/api/v1/lesson/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isUnauthorized());
    }

    // TC_LESSON_API_004
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_LESSON_API_004: POST /api/v1/lesson/create returns 403 for STUDENT role")
    void createLessonForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/lesson/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isForbidden());
    }

    // TC_LESSON_API_005
    @Test
    @WithMockUser(roles = "CONSULTANT")
    @DisplayName("TC_LESSON_API_005: POST /api/v1/lesson/create returns 200 for CONSULTANT role")
    void createLessonAllowedForConsultant() throws Exception {
        when(lessonService.createLesson(any(), any())).thenReturn(new LessonResponse());

        mockMvc.perform(post("/api/v1/lesson/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isOk());
    }

    // TC_LESSON_API_006
    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("TC_LESSON_API_006: POST /api/v1/lesson/create returns 200 for MANAGER role")
    void createLessonAllowedForManager() throws Exception {
        when(lessonService.createLesson(any(), any())).thenReturn(new LessonResponse());

        mockMvc.perform(post("/api/v1/lesson/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isOk());
    }

    // TC_LESSON_API_007
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_LESSON_API_007: POST /api/v1/lesson/get-own-lessons returns 403 for STUDENT")
    void getOwnLessonsForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/lesson/get-own-lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchBaseDto())))
                .andExpect(status().isForbidden());
    }

    // TC_LESSON_API_008
    @Test
    @WithMockUser(roles = "CONSULTANT")
    @DisplayName("TC_LESSON_API_008: POST /api/v1/lesson/get-own-lessons returns 200 for CONSULTANT")
    void getOwnLessonsAllowedForConsultant() throws Exception {
        when(lessonService.getOwnLessons(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post("/api/v1/lesson/get-own-lessons")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SearchBaseDto())))
                .andExpect(status().isOk());
    }

    // TC_LESSON_API_009
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("TC_LESSON_API_009: POST /api/v1/lesson/update-info returns 403 for STUDENT")
    void updateLessonForbiddenForStudent() throws Exception {
        mockMvc.perform(post("/api/v1/lesson/update-info")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isForbidden());
    }

    // TC_LESSON_API_010
    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("TC_LESSON_API_010: POST /api/v1/lesson/update-status returns 200 for MANAGER")
    void updateStatusAllowedForManager() throws Exception {
        when(lessonService.disableOrDelete(any(), any())).thenReturn(new LessonResponse());

        mockMvc.perform(post("/api/v1/lesson/update-status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LessonRequest())))
                .andExpect(status().isOk());
    }
}
