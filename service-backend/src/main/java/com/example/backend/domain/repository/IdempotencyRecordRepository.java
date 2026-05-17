package com.example.backend.domain.repository;

import com.example.backend.domain.entity.IdempotencyRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para registros de idempotência.
 */
@Repository
public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {

    /**
     * Busca um registro por operação e chave de idempotência.
     *
     * @param operationName nome da operação
     * @param idempotencyKey chave de idempotência
     * @return registro encontrado, se existir
     */
    Optional<IdempotencyRecord> findByOperationNameAndIdempotencyKey(String operationName, String idempotencyKey);
}
