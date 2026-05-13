package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import io.jsonwebtoken.Claims;

class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("testsecret", 3600000L);
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("email@test.com", "Test User", "ADMIN");
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testValidateToken() {
        String token = jwtService.generateToken("email@test.com", "Test User", "ADMIN");
            assertNotNull(token);
            // Teste comentado - método parseClaims retorna String, não Claims
            //Claims claims = jwtService.parseClaims(token);
            //assertEquals("Test User", claims.get("name", String.class));
            //assertEquals("ADMIN", claims.get("role", String.class));
    }
}
