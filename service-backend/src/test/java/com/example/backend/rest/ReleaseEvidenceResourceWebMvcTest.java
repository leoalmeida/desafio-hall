package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.config.SecurityConfig;
import com.example.backend.dto.EvidenceScoreResponseDto;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.ReleaseAuditService;
import com.example.backend.service.ReleaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReleaseEvidenceResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ReleaseEvidenceResourceWebMvcTest {

        private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReleaseService releaseService;

    @MockBean
    private ReleaseAuditService releaseAuditService;

    @MockBean
    private JwtService jwtService;

    private static final String VALID_TOKEN = "valid-token";
    private static final String AUTH_HEADER = "Bearer " + VALID_TOKEN;

    private void mockAdminToken() {
        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn("admin@test.com");
        when(jwtService.extractRole(VALID_TOKEN)).thenReturn("ADMIN");
    }

    private void mockViewerToken() {
        when(jwtService.isTokenValid(VALID_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(VALID_TOKEN)).thenReturn("viewer@test.com");
        when(jwtService.extractRole(VALID_TOKEN)).thenReturn("VIEWER");
    }

    @Test
    void updateEvidenceUrlDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        ReleaseEvidenceUpdateRequestDto request = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl("https://example.com/evidence-updated")
                .build();
        ReleaseResponseDto response = ReleaseResponseDto.builder()
                .id(RELEASE_ID)
                .applicationId(APPLICATION_ID)
                .version("V1.0")
                .env("PROD")
                .status("CREATED")
                .evidenceUrl("https://example.com/evidence-updated")
                .build();
        when(releaseService.updateEvidenceUrl(eq(RELEASE_ID), any(ReleaseEvidenceUpdateRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/releases/" + RELEASE_ID + "/evidence-url")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidenceUrl").value("https://example.com/evidence-updated"));

        verify(releaseService).updateEvidenceUrl(eq(RELEASE_ID), any(ReleaseEvidenceUpdateRequestDto.class));
        verify(releaseAuditService).logChangeEvidenceUrl(eq(RELEASE_ID), any(ReleaseEvidenceUpdateRequestDto.class));
    }

    @Test
    void updateEvidenceUrlDeveRetornar403ComTokenViewer() throws Exception {
        mockViewerToken();
        ReleaseEvidenceUpdateRequestDto request = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl("https://example.com/evidence-updated")
                .build();

        mockMvc.perform(patch("/api/releases/" + RELEASE_ID + "/evidence-url")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(releaseService, never()).updateEvidenceUrl(any(UUID.class), any(ReleaseEvidenceUpdateRequestDto.class));
    }

    @Test
    void evidenceScoreDeveRetornar200ComTokenViewer() throws Exception {
        mockViewerToken();
        EvidenceScoreResponseDto response = EvidenceScoreResponseDto.builder()
                .releaseId(RELEASE_ID)
                .score(88)
                .evidenceUrl("https://ci.example.com/reports/rel-1?result=PASS")
                .rationale("validUrl=true")
                .build();
        when(releaseService.calculateEvidenceScore(RELEASE_ID)).thenReturn(response);

        mockMvc.perform(get("/api/releases/" + RELEASE_ID + "/evidence-score")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseId").value(RELEASE_ID.toString()))
                .andExpect(jsonPath("$.score").value(88));

        verify(releaseAuditService).logEvidenceScore(RELEASE_ID);
    }

    @Test
    void evidenceScoreDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/releases/" + RELEASE_ID + "/evidence-score"))
                .andExpect(status().isUnauthorized());
    }
}
