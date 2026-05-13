package com.example.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backend.config.SecurityConfig;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.rest.AuditLogResource;
import com.example.backend.rest.AuthResource;
import com.example.backend.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {AuthResource.class, AuditLogResource.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuditLogService auditLogService;

        @MockBean
        private AuditLogManager auditLogManager;

    @MockBean
    private JwtService jwtService;

    @Test
    void deveRealizarLoginComSenhaHash() throws Exception {
        User admin = User.builder()
                .email("admin@email.com")
                .name("Admin")
                .role("ADMIN")
                .pawd(passwordEncoder.encode("123456"))
                .status("ativo")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));
        when(jwtService.generateToken("admin@email.com", "Admin", "ADMIN")).thenReturn("token-admin");

        String body = objectMapper.writeValueAsString(new LoginRequest("admin@email.com", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-admin"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveMigrarSenhaLegadaParaHashBcryptNoLoginValido() throws Exception {
        User legacyUser = User.builder()
                .email("user@email.com")
                .name("Usuario")
                .role("USER")
                .pawd("123456")
                .status("ativo")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(legacyUser));
        when(jwtService.generateToken("user@email.com", "Usuario", "USER")).thenReturn("token-user");

        String body = objectMapper.writeValueAsString(new LoginRequest("user@email.com", "123456"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-user"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        String persistedPassword = userCaptor.getValue().getPawd();
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("123456", persistedPassword));
    }

    @Test
    void deveRetornar401QuandoCredenciaisForemInvalidas() throws Exception {
        User admin = User.builder()
                .email("admin@email.com")
                .name("Admin")
                .role("ADMIN")
                .pawd(passwordEncoder.encode("123456"))
                .status("ativo")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));

        String body = objectMapper.writeValueAsString(new LoginRequest("admin@email.com", "senha-errada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoAcessarEndpointProtegidoSemToken() throws Exception {
        mockMvc.perform(get("/api/audit")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar403QuandoUsuarioSemRoleAdminAcessarEndpointAdmin() throws Exception {
        when(jwtService.isTokenValid("token-user")).thenReturn(true);
        when(jwtService.extractEmail("token-user")).thenReturn("user@email.com");
        when(jwtService.extractRole("token-user")).thenReturn("USER");

        mockMvc.perform(get("/api/audit").header(HttpHeaders.AUTHORIZATION, "Bearer token-user"))
                .andExpect(status().isForbidden());

        verify(auditLogService, never()).findAll();
    }

    private record LoginRequest(String email, String password) {}
}
