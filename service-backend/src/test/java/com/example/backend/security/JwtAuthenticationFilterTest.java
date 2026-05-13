package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Testes para JwtAuthenticationFilter.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilterTest")
class JwtAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private String jwtToken;
    private String email;
    private String role;

    @BeforeEach
    void setUp() {
        jwtToken = "Bearer jwt.token.here";
        email = "user@example.com";
        role = "ADMIN";
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal com token válido deve autenticar usuário")
    void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.isTokenValid(jwtToken.substring(7))).thenReturn(true);
        when(jwtService.extractEmail(jwtToken.substring(7))).thenReturn(email);
        when(jwtService.extractRole(jwtToken.substring(7))).thenReturn(role);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).isTokenValid(jwtToken.substring(7));
    }

    @Test
    @DisplayName("doFilterInternal sem token deve prosseguir sem autenticar")
    void testDoFilterInternalWithoutToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("doFilterInternal com token inválido deve prosseguir sem autenticar")
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.isTokenValid(jwtToken.substring(7))).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
    }

    @Test
    @DisplayName("doFilterInternal com header vazio deve prosseguir sem autenticar")
    void testDoFilterInternalWithEmptyAuthHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal com prefixo não-Bearer deve prosseguir sem autenticar")
    void testDoFilterInternalWithoutBearerPrefix() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    @DisplayName("doFilterInternal deve chamar filterChain mesmo com erro")
    void testDoFilterInternalCallsFilterChainOnError() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.isTokenValid(anyString())).thenThrow(new RuntimeException("JWT parsing error"));

        assertThrows(RuntimeException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    @DisplayName("doFilterInternal com token válido deve extrair email")
    void testDoFilterInternalExtractsEmail() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.isTokenValid(jwtToken.substring(7))).thenReturn(true);
        when(jwtService.extractEmail(jwtToken.substring(7))).thenReturn(email);
        when(jwtService.extractRole(jwtToken.substring(7))).thenReturn(role);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService, times(1)).extractEmail(jwtToken.substring(7));
        verify(jwtService, times(1)).extractRole(jwtToken.substring(7));
    }

    @Test
    @DisplayName("doFilterInternal deve continuar mesmo com claims null")
    void testDoFilterInternalWithNullClaims() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(jwtToken);
        when(jwtService.isTokenValid(jwtToken.substring(7))).thenReturn(true);
        when(jwtService.extractEmail(jwtToken.substring(7))).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
