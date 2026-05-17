package com.example.backend.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.dto.AuthResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.service.AuthService;

@WebMvcTest(controllers = AuthResource.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthResourceWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuditLogManager auditLogManager;

    @Test
    void loginDeveRetornar200ComTokenQuandoRequestValido() throws Exception {
        when(authService.loginUser(any()))
                .thenReturn(AuthResponseDto.builder().token("jwt-token").tokenType("Bearer").build());

        String body = """
                {
                  \"email\": \"user@email.com\",
                  \"pawd\": \"123456\"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(auditLogManager).logAction(
                eq("user@email.com"),
                eq("LOGIN"),
                eq("AppUser"),
                isNull(),
                argThat(payload -> payload != null && payload.contains("user@email.com")));
        verify(authService).loginUser(any());
    }

    @Test
    void loginDeveRetornar400QuandoBodyInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest());
    }
}
