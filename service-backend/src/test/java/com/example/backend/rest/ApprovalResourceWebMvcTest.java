package com.example.backend.rest;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.SecurityConfig;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.dto.ApprovalResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.ApprovalService;

@WebMvcTest(controllers = ApprovalResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ApprovalResourceWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

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

    private ApprovalResponseDto buildApprovalDto(final Long id, final String outcome) {
        return ApprovalResponseDto.builder()
                .id(id)
                .releaseId(10L)
                .approverEmail("approver@test.com")
                .outcome(outcome)
                .timestamp("2026-01-01T12:00:00")
                .build();
    }

    // --- GET /api/approvals ---

    @Test
    void findAllDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/approvals"))
                .andExpect(status().isUnauthorized());

        verify(approvalService, never()).findAll();
    }

    @Test
    void findAllDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/approvals").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());

        verify(approvalService, never()).findAll();
    }

    @Test
    void findAllDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(approvalService.findAll()).thenReturn(List.of(buildApprovalDto(1L, "APPROVED")));

        mockMvc.perform(get("/api/approvals").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].outcome").value("APPROVED"));

        verify(approvalService).findAll();
    }

    // --- GET /api/approvals/approver ---

    @Test
    void findByApproverDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(approvalService.findByApprover("approver@test.com"))
                .thenReturn(List.of(buildApprovalDto(2L, "REJECTED")));

        mockMvc.perform(get("/api/approvals/approver")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("aprovador", "approver@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        verify(approvalService).findByApprover("approver@test.com");
    }

    @Test
    void findByApproverDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/approvals/approver")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("aprovador", "approver@test.com"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/approvals/release ---

    @Test
    void findByReleaseDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(approvalService.findByReleaseId(10L))
                .thenReturn(List.of(buildApprovalDto(3L, "APPROVED")));

        mockMvc.perform(get("/api/approvals/release")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("releaseId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].releaseId").value(10));

        verify(approvalService).findByReleaseId(10L);
    }

    // --- GET /api/approvals/outcome ---

    @Test
    void findByOutcomeDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(approvalService.findByOutcome(OutcomeEnum.APPROVED))
                .thenReturn(List.of(buildApprovalDto(4L, "APPROVED")));

        mockMvc.perform(get("/api/approvals/outcome")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("outcome", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].outcome").value("APPROVED"));

        verify(approvalService).findByOutcome(OutcomeEnum.APPROVED);
    }

    @Test
    void findByOutcomeDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/approvals/outcome")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("outcome", "APPROVED"))
                .andExpect(status().isForbidden());
    }
}
