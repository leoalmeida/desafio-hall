package com.example.backend.service;

/**
 * Resultado do processamento de uma Idempotency-Key.
 */
public record IdempotencyResult(IdempotencyState state, Integer responseStatus) {

    /**
     * Indica se o request deve seguir para processamento.
     *
     * @return true quando a operação deve ser executada
     */
    public boolean shouldProcess() {
        return state == IdempotencyState.NEW;
    }
}
