package com.example.backend.service;

/**
 * Estados possíveis de uma chave de idempotência.
 */
public enum IdempotencyState {
    NEW,
    IN_PROGRESS,
    COMPLETED
}
