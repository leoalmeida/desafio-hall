package com.example.backend.dto;

/**
 * Constantes de validação para DTOs.
 */
public final class DtoConstants {

    // Tamanhos de campo
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_OUTCOME_LENGTH = 50;
    public static final int MAX_NOTES_LENGTH = 1000;
    public static final int MAX_VERSION_LENGTH = 50;
    public static final int MAX_TEXT_LENGTH = 255;
    public static final int MIN_ID_VALUE = 1;

    private DtoConstants() {
        throw new IllegalStateException("Utility class");
    }
}
