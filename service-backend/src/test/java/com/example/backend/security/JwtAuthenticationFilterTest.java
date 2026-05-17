package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextUtils.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextUtils.clearContext();
    }

    @Test
    void deveIgnorarQuandoAuthorizationAusente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextUtils.getAuthentication());
    }

    @Test
    void deveAutenticarQuandoTokenValidoESemContextoPrevio() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.isTokenValid("token-valido")).thenReturn(true);
        when(jwtService.extractEmail("token-valido")).thenReturn("admin@email.com");
        when(jwtService.extractRole("token-valido")).thenReturn("admin");

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextUtils.getAuthentication();
        assertNotNull(auth);
        assertEquals("admin@email.com", auth.getPrincipal());
        assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());

        verify(jwtService).isTokenValid("token-valido");
        verify(jwtService).extractEmail("token-valido");
        verify(jwtService).extractRole("token-valido");
    }

    @Test
    void naoDeveSobrescreverAutenticacaoExistente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        SecurityContextUtils.setAuthentication(
                new UsernamePasswordAuthenticationToken("existing@email.com", null));
        when(jwtService.isTokenValid("token-valido")).thenReturn(true);

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextUtils.getAuthentication();
        assertNotNull(auth);
        assertEquals("existing@email.com", auth.getPrincipal());
    }

    @Test
    void deveLimparContextoQuandoTokenLancarJwtException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-quebrado");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        SecurityContextUtils.setAuthentication(
                new UsernamePasswordAuthenticationToken("existing@email.com", null));

        when(jwtService.isTokenValid("token-quebrado")).thenThrow(new JwtException("token inválido"));

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextUtils.getAuthentication());
    }
}
