package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.SecurityConfig;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.ReleaseService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ReleaseResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ReleaseResourceWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReleaseService releaseService;

    @MockBean
    private AuditLogManager auditLogManager;

    @MockBean
    private JwtService jwtService;

    private static final String VALID_TOKEN = "valid-token";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;

    private void mockAdminToken() {
        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn("admin@test.com");
        when(jwtService.extractRole(VALID_TOKEN)).thenReturn("ADMIN");
    }

    private void mockUserToken() {
        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn("user@test.com");
        when(jwtService.extractRole(VALID_TOKEN)).thenReturn("USER");
    }

    // --- GET /api/releases ---

    @Test
    void findDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/releases")
                        .param("applicationId", "1")
                        .param("version", "V1.0")
                        .param("environment", "PROD")
                        .param("status", "CREATED"))
                .andExpect(status().isUnauthorized());

        verify(releaseService, never()).find(anyLong(), any(), any(), any());
    }

    @Test
    void findDeveRetornar200ComTokenUser() throws Exception {
        mockUserToken();
        ReleaseResponseDto dto = ReleaseResponseDto.builder()
                .id(1L)
                .applicationId(1L)
                .version("V1.0")
                .env("PROD")
                .status("CREATED")
                .build();
        when(releaseService.find(eq(1L), eq("V1.0"), eq(EnvironmentEnum.PROD), eq(StatusEnum.CREATED)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/releases")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("applicationId", "1")
                        .param("version", "V1.0")
                        .param("environment", "PROD")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].version").value("V1.0"));
    }

    // --- POST /api/releases ---

    @Test
    void createDeveRetornar401SemToken() throws Exception {
        ReleaseRequestDto request = ReleaseRequestDto.builder()
                .applicationId(1L)
                .version("V1.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.CREATED)
                .build();

        mockMvc.perform(post("/api/releases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();
        ReleaseRequestDto request = ReleaseRequestDto.builder()
                .applicationId(1L)
                .version("V1.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.CREATED)
                .build();

        mockMvc.perform(post("/api/releases")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(releaseService, never()).create(any());
    }

    @Test
    void createDeveRetornar201ComTokenAdmin() throws Exception {
        mockAdminToken();
        ReleaseRequestDto request = ReleaseRequestDto.builder()
                .applicationId(1L)
                .version("V1.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.CREATED)
                .evidenceUrl("https://evidence.example.com")
                .build();
        ReleaseResponseDto response = ReleaseResponseDto.builder()
                .id(10L)
                .applicationId(1L)
                .version("V1.0")
                .env("PROD")
                .status("CREATED")
                .build();
        when(releaseService.create(any(ReleaseRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/releases")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(releaseService).create(any(ReleaseRequestDto.class));
    }

    // --- POST /api/releases/{id}/approve ---

    @Test
    void approveDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(post("/api/releases/1/approve")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());

        verify(releaseService, never()).approveRelease(anyLong(), any(OutcomeEnum.class));
    }

    @Test
    void approveDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

        mockMvc.perform(post("/api/releases/1/approve")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

        verify(releaseService).approveRelease(1L, OutcomeEnum.APPROVED);
    }

    // --- POST /api/releases/{id}/disapprove ---

    @Test
    void disapproveDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

        mockMvc.perform(post("/api/releases/1/disapprove")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

        verify(releaseService).approveRelease(1L, OutcomeEnum.REJECTED);
    }

    @Test
    void disapproveDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(post("/api/releases/1/disapprove")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/releases/{id}/promote ---

    @Test
    void promoteDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

        mockMvc.perform(post("/api/releases/1/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

        verify(releaseService).promoteRelease(1L);
    }

    @Test
    void promoteDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(post("/api/releases/1/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());
    }
}
