package com.example.backend.exception;

import java.time.Instant;
import lombok.NoArgsConstructor;

/* Classe de modelo para representar erros de resposta. */
@NoArgsConstructor
public class ResponseError {
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_SUCCESS = "success";
    public static final int STATUS_CODE_SUCCESS = 200;
    public static final int STATUS_CODE_ERROR = 400;

    private Instant timestamp = Instant.now();
    private String status = STATUS_ERROR;
    private int statusCode = STATUS_CODE_ERROR;
    private String error;

    public ResponseError setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public ResponseError setError(final String error) {
        this.error = error;
        return this;
    }

    /**
     * @return the timestamp
     */
    String getTimestamp() {
        return timestamp.toString();
    }

    /**
     * @return the error
     */
    String getError() {
        return error;
    }

    /**
     * @return the statusCode
     */
    int getStatusCode() {
        return statusCode;
    }

    /**
     * @return the status
     */
    String getStatus() {
        return status;
    }
}
