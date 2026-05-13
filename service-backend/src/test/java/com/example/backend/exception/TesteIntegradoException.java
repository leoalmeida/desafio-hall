package com.example.backend.exception;

public class TesteIntegradoException extends RuntimeException {
    public TesteIntegradoException(String message) {
        super(message);
    }

    public TesteIntegradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
