package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.security.JwtAuthenticationFilter;
import com.example.backend.security.JwtService;
import com.example.backend.service.ApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ApplicationResource.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ApplicationResourceWebMvcTest {

        private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
        private static final UUID APPLICATION_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

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

    // --- GET /api/applications ---

    @Test
    void findAllApplicationsDeveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());

        verify(applicationService, never()).findAll();
    }

    @Test
    void findAllApplicationsDeveRetornar200ComTokenValido() throws Exception {
        mockAdminToken();
        ApplicationResponseDto dto = ApplicationResponseDto.builder()
                                .id(APPLICATION_ID)
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://repo.example.com")
                .build();
        when(applicationService.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APPLICATION_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("app-core"));

        verify(applicationService).findAll();
    }

    @Test
        void findAllApplicationsDeveRetornar403ComTokenUser() throws Exception {
        mockUserToken();
        when(applicationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/applications").header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                                .andExpect(status().isForbidden());
    }

    // --- POST /api/applications ---

    @Test
    void createApplicationDeveRetornar201QuandoPayloadValido() throws Exception {
        mockAdminToken();
        ApplicationRequestDto request = ApplicationRequestDto.builder()
                .name("nova-app")
                .ownerTeam("Team B")
                .repoUrl("https://repo.example.com/nova")
                .build();
        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(APPLICATION_ID_2)
                .name("nova-app")
                .ownerTeam("Team B")
                .build();
        when(applicationService.create(any(ApplicationRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(APPLICATION_ID_2.toString()))
                .andExpect(jsonPath("$.name").value("nova-app"));

        verify(applicationService).create(any(ApplicationRequestDto.class));
    }

    @Test
    void createApplicationDeveRetornar401SemToken() throws Exception {
        ApplicationRequestDto request = ApplicationRequestDto.builder().name("app").build();

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- PUT /api/applications/{id} ---

    @Test
    void putApplicationDeveRetornar200QuandoValido() throws Exception {
        mockAdminToken();
        ApplicationRequestDto request = ApplicationRequestDto.builder()
                .name("updated")
                .ownerTeam("Team C")
                .repoUrl("https://repo.example.com/updated")
                .build();
        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(APPLICATION_ID)
                .name("updated")
                .build();
        when(applicationService.update(any(UUID.class), any(ApplicationRequestDto.class), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(put("/api/applications/" + APPLICATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated"));
    }

    // --- PATCH /api/applications/{id} ---

    @Test
    void patchApplicationDeveRetornar200QuandoValido() throws Exception {
        mockAdminToken();
        ApplicationRequestDto request = ApplicationRequestDto.builder()
                .name("patched-name")
                .build();
        ApplicationResponseDto response = ApplicationResponseDto.builder()
                .id(APPLICATION_ID)
                .name("patched-name")
                .ownerTeam("Team A")
                .build();
        when(applicationService.update(any(UUID.class), any(ApplicationRequestDto.class), anyBoolean()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/applications/" + APPLICATION_ID)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("patched-name"));
    }
}
