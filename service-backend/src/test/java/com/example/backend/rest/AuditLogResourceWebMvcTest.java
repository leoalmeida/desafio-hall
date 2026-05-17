package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.SecurityConfig;
import com.example.backend.dto.AuditLogRequestDto;
import com.example.backend.dto.AuditLogResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuditLogResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuditLogResourceWebMvcTest {

        private static final UUID AUDIT_LOG_ID_1 = UUID.fromString("30000000-0000-0000-0000-000000000001");
        private static final UUID AUDIT_LOG_ID_3 = UUID.fromString("30000000-0000-0000-0000-000000000003");
        private static final UUID AUDIT_LOG_ID_4 = UUID.fromString("30000000-0000-0000-0000-000000000004");
        private static final UUID AUDIT_LOG_ID_5 = UUID.fromString("30000000-0000-0000-0000-000000000005");
        private static final UUID AUDIT_LOG_ID_6 = UUID.fromString("30000000-0000-0000-0000-000000000006");
        private static final UUID AUDIT_LOG_ID_7 = UUID.fromString("30000000-0000-0000-0000-000000000007");
        private static final String RELEASE_ID = "10000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditLogService auditLogService;

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

        private AuditLogResponseDto buildLogDto(final UUID id) {
        return AuditLogResponseDto.builder()
                .id(id)
                .actor("admin@test.com")
                .action("CREATE")
                .entity("Release")
                                .entityId(RELEASE_ID)
                .timestamp("2026-01-01T12:00:00")
                .build();
    }

    // --- GET /api/audit ---

    @Test
    void findAllDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/audit"))
                .andExpect(status().isUnauthorized());

        verify(auditLogService, never()).findAll();
    }

    @Test
    void findAllDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/audit").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isForbidden());

        verify(auditLogService, never()).findAll();
    }

    @Test
    void findAllDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(auditLogService.findAll()).thenReturn(List.of(buildLogDto(AUDIT_LOG_ID_1)));

        mockMvc.perform(get("/api/audit").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(AUDIT_LOG_ID_1.toString()))
                .andExpect(jsonPath("$[0].action").value("CREATE"));

        verify(auditLogService).findAll();
    }

        @Test
        void findAllDeveAplicarFiltroPorAcaoNaRotaRaiz() throws Exception {
                mockAdminToken();
                when(auditLogService.findByAction("CREATE"))
                                .thenReturn(List.of(buildLogDto(AUDIT_LOG_ID_7)));

                mockMvc.perform(get("/api/audit")
                                                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                                                .param("acao", "CREATE"))
                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$[0].id").value(AUDIT_LOG_ID_7.toString()))
                                .andExpect(jsonPath("$[0].action").value("CREATE"));

                verify(auditLogService).findByAction("CREATE");
        }

        @Test
        void findAllDeveRetornar400QuandoIntervaloVierIncompletoNaRotaRaiz() throws Exception {
                mockAdminToken();

                mockMvc.perform(get("/api/audit")
                                                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                                                .param("dataInicio", "2026-01-01T00:00:00"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
        }

    // --- GET /api/audit/actor ---

    @Test
    void findByActorDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(auditLogService.findByActor("admin@test.com"))
                                .thenReturn(List.of(buildLogDto(UUID.fromString("30000000-0000-0000-0000-000000000002"))));

        mockMvc.perform(get("/api/audit/actor")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("ator", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actor").value("admin@test.com"));

        verify(auditLogService).findByActor("admin@test.com");
    }

    @Test
    void findByActorDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/audit/actor")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("ator", "admin@test.com"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/audit/action ---

    @Test
    void findByActionDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(auditLogService.findByAction("CREATE"))
                                .thenReturn(List.of(buildLogDto(AUDIT_LOG_ID_3)));

        mockMvc.perform(get("/api/audit/action")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("acao", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE"));

        verify(auditLogService).findByAction("CREATE");
    }

    @Test
    void findByActionDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/audit/action")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("acao", "CREATE"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/audit/entity ---

    @Test
    void findByEntityDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(auditLogService.findByEntity("Release"))
                                .thenReturn(List.of(buildLogDto(AUDIT_LOG_ID_4)));

        mockMvc.perform(get("/api/audit/entity")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("entidade", "Release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entity").value("Release"));

        verify(auditLogService).findByEntity("Release");
    }

    @Test
    void findByEntityDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();

        mockMvc.perform(get("/api/audit/entity")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("entidade", "Release"))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/audit ---

    @Test
    void createDeveRetornar401SemToken() throws Exception {
        AuditLogRequestDto request = AuditLogRequestDto.builder()
                .actor("admin@test.com")
                .action("CREATE")
                .entity("Release")
                                .entityId(RELEASE_ID)
                .build();

        mockMvc.perform(post("/api/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();
        AuditLogRequestDto request = AuditLogRequestDto.builder()
                .actor("admin@test.com")
                .action("CREATE")
                .entity("Release")
                                .entityId(RELEASE_ID)
                .build();

        mockMvc.perform(post("/api/audit")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(auditLogService, never()).create(any());
    }

    @Test
    void createDeveRetornar201ComTokenAdmin() throws Exception {
        mockAdminToken();
        AuditLogRequestDto request = AuditLogRequestDto.builder()
                .actor("admin@test.com")
                .action("CREATE")
                .entity("Release")
                .entityId(RELEASE_ID)
                .payload("{\"version\":\"V1.0\"}")
                .build();
        AuditLogResponseDto response = buildLogDto(AUDIT_LOG_ID_5);
        when(auditLogService.create(any(AuditLogRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/audit")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(AUDIT_LOG_ID_5.toString()));

        verify(auditLogService).create(any(AuditLogRequestDto.class));
    }

    // --- GET /api/audit/interval ---

    @Test
    void findByDateRangeDeveRetornar200ComTokenAdmin() throws Exception {
        mockAdminToken();
        when(auditLogService.findByDateRange(any(), any()))
                                .thenReturn(List.of(buildLogDto(AUDIT_LOG_ID_6)));

        mockMvc.perform(get("/api/audit/interval")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .param("dataInicio", "2026-01-01T00:00:00")
                        .param("dataFim", "2026-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(AUDIT_LOG_ID_6.toString()));

        verify(auditLogService).findByDateRange(any(), any());
    }
}
