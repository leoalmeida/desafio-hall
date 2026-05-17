package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.SecurityConfig;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.ReleaseAuditService;
import com.example.backend.service.ReleasePromotionCoordinator;
import com.example.backend.service.ReleaseService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ReleaseResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ReleaseResourceWebMvcTest {

        private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
        private static final UUID RELEASE_ID_10 = UUID.fromString("10000000-0000-0000-0000-000000000010");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReleaseService releaseService;

    @MockBean
        private ReleaseAuditService releaseAuditService;

        @MockBean
        private ReleasePromotionCoordinator releasePromotionCoordinator;

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
                when(jwtService.extractRole(VALID_TOKEN)).thenReturn("VIEWER");
    }

    // --- GET /api/releases ---

    @Test
    void findDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/releases")
                                                .param("applicationId", APPLICATION_ID.toString())
                        .param("version", "V1.0")
                        .param("environment", "PROD")
                        .param("status", "CREATED"))
                .andExpect(status().isUnauthorized());

                verify(releaseService, never()).find(any(UUID.class), any(), any(), any());
    }

    @Test
    void findDeveRetornar200ComTokenUser() throws Exception {
        mockUserToken();
        ReleaseResponseDto dto = ReleaseResponseDto.builder()
                .id(RELEASE_ID)
                .applicationId(APPLICATION_ID)
                .version("V1.0")
                .env("PROD")
                .status("CREATED")
                .build();
        when(releaseService.find(eq(APPLICATION_ID), eq("V1.0"), eq(EnvironmentEnum.PROD), eq(StatusEnum.CREATED)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/releases")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("applicationId", APPLICATION_ID.toString())
                        .param("version", "V1.0")
                        .param("environment", "PROD")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RELEASE_ID.toString()))
                .andExpect(jsonPath("$[0].version").value("V1.0"));
    }

    // --- POST /api/releases ---

    @Test
    void createDeveRetornar401SemToken() throws Exception {
        ReleaseRequestDto request = ReleaseRequestDto.builder()
                                .applicationId(APPLICATION_ID)
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
                                .applicationId(APPLICATION_ID)
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
                .applicationId(APPLICATION_ID)
                .version("V1.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.CREATED)
                .evidenceUrl("https://evidence.example.com")
                .build();
        ReleaseResponseDto response = ReleaseResponseDto.builder()
                .id(RELEASE_ID_10)
                .applicationId(APPLICATION_ID)
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
                .andExpect(jsonPath("$.id").value(RELEASE_ID_10.toString()));

        verify(releaseService).create(any(ReleaseRequestDto.class));
    }

    // --- POST /api/releases/{id}/approve ---

    @Test
    void approveDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());

                verify(releaseService, never()).approveRelease(any(UUID.class), any(OutcomeEnum.class));
    }

    @Test
    void approveDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

                verify(releaseService).approveRelease(RELEASE_ID, OutcomeEnum.APPROVED);
    }

    // --- POST /api/releases/{id}/disapprove ---

    @Test
    void disapproveDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/disapprove")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

                verify(releaseService).approveRelease(RELEASE_ID, OutcomeEnum.REJECTED);
    }

    @Test
    void disapproveDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/disapprove")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/releases/{id}/promote ---

    @Test
    void promoteDeveRetornar204ComTokenAdmin() throws Exception {
        mockAdminToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isNoContent());

                verify(releasePromotionCoordinator).promote(RELEASE_ID, null);
    }

    @Test
    void promoteDeveRetornar204SemDuplicarQuandoIdempotencyKeyJaConcluida() throws Exception {
        mockAdminToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .header("Idempotency-Key", "key-1"))
                .andExpect(status().isNoContent());

                verify(releasePromotionCoordinator).promote(RELEASE_ID, "key-1");
    }

    @Test
    void promoteDeveRetornar409QuandoIdempotencyKeyEstiverEmProcessamento() throws Exception {
        mockAdminToken();
        doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "Idempotency-Key já está em processamento"))
                        .when(releasePromotionCoordinator)
                        .promote(RELEASE_ID, "key-2");

        mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .header("Idempotency-Key", "key-2"))
                .andExpect(status().isConflict());

        verify(releasePromotionCoordinator).promote(RELEASE_ID, "key-2");
    }

    @Test
    void promoteDeveConcluirRegistroQuandoIdempotencyKeyForNova() throws Exception {
        mockAdminToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .header("Idempotency-Key", "key-3"))
                .andExpect(status().isNoContent());

                verify(releasePromotionCoordinator).promote(RELEASE_ID, "key-3");
    }

    @Test
    void promoteDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

                mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());
    }

    @Test
    void promoteDeveRetornar409QuandoHouverConcorrencia() throws Exception {
        mockAdminToken();
        doThrow(new ObjectOptimisticLockingFailureException("Release", RELEASE_ID))
                .when(releasePromotionCoordinator)
                .promote(RELEASE_ID, null);

        mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void promoteDeveRetornar422QuandoPolicyBloquearPromocao() throws Exception {
        mockAdminToken();
        doThrow(new BusinessException("Janela de freeze ativa para ambiente PROD"))
                .when(releasePromotionCoordinator)
                .promote(RELEASE_ID, null);

        mockMvc.perform(post("/api/releases/" + RELEASE_ID + "/promote")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

}
