package com.example.backend.rest;

/**
 * Constantes de configuração para REST.
 */
public final class RestConstants {

    // Configuração CORS
    public static final long CORS_MAX_AGE = 3600L;

    private RestConstants() {
        throw new IllegalStateException("Utility class");
    }
}
