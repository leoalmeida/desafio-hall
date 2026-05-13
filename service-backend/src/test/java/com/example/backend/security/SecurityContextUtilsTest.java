package com.example.backend.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SecurityContextUtilsTest {
    @Test
    void testGetCurrentUserEmail() {
        // Simule o contexto de segurança conforme necessário
        String email = SecurityContextUtils.getCurrentUserEmail();
        assertNull(email); // Esperado null sem contexto
    }
}
