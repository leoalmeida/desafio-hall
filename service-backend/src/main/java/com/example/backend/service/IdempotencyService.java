package com.example.backend.service;

import java.util.UUID;

/**
 * Serviço de deduplicação por Idempotency-Key.
 */
public interface IdempotencyService {

    /**
     * Tenta registrar ou reutilizar uma chave de idempotência para uma operação.
     *
     * @param operationName nome da operação
     * @param idempotencyKey chave recebida no header
     * @param resourceId identificador do recurso alvo
     * @param requestHash impressão digital determinística da requisição
     * @return resultado da tentativa de registro
     */
    IdempotencyResult begin(String operationName, String idempotencyKey, UUID resourceId, String requestHash);

    /**
     * Marca a operação como concluída.
     *
     * @param operationName nome da operação
     * @param idempotencyKey chave recebida no header
     * @param responseStatus status HTTP persistido para replays futuros
     */
    void complete(String operationName, String idempotencyKey, int responseStatus);

    /**
     * Remove o registro pendente quando a operação falha.
     *
     * @param operationName nome da operação
     * @param idempotencyKey chave recebida no header
     */
    void abort(String operationName, String idempotencyKey);
}
