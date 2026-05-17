package com.example.backend.exception;

import java.time.Instant;

import lombok.NoArgsConstructor;

/* Classe de modelo para representar erros de resposta. */
@NoArgsConstructor
public class ResponseError {
    private final Instant timestamp = Instant.now();
    private String code;
    private String message;
    private Object details;

    public ResponseError setCode(final String code) {
        this.code = code;
        return this;
    }

    public ResponseError setMessage(final String message) {
        this.message = message;
        return this;
    }

    public ResponseError setDetails(final Object details) {
        this.details = details;
        return this;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getDetails() {
        return details;
    }
}
