package com.example.backend.service.impl;

import com.example.backend.domain.entity.IdempotencyRecord;
import com.example.backend.domain.repository.IdempotencyRecordRepository;
import com.example.backend.service.IdempotencyResult;
import com.example.backend.service.IdempotencyService;
import com.example.backend.service.IdempotencyState;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Implementação do serviço de idempotência.
 */
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final int DEFAULT_COMPLETED_STATUS = 204;

    private final IdempotencyRecordRepository repository;

    public IdempotencyServiceImpl(final IdempotencyRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository não pode ser nulo");
    }

    @Override
    @Transactional
    public IdempotencyResult begin(
            final String operationName,
            final String idempotencyKey,
            final UUID resourceId,
            final String requestHash) {
        validateInputs(operationName, idempotencyKey, resourceId, requestHash);

        IdempotencyRecord existing = repository
                .findByOperationNameAndIdempotencyKey(operationName, idempotencyKey)
                .orElse(null);
        if (existing != null) {
            return toResult(existing, resourceId, requestHash);
        }

        try {
            repository.saveAndFlush(buildPendingRecord(operationName, idempotencyKey, resourceId, requestHash));
            return new IdempotencyResult(IdempotencyState.NEW, null);
        } catch (DataIntegrityViolationException ex) {
            IdempotencyRecord persisted = repository
                    .findByOperationNameAndIdempotencyKey(operationName, idempotencyKey)
                    .orElseThrow(() -> ex);
            return toResult(persisted, resourceId, requestHash);
        }
    }

    @Override
    @Transactional
    public void complete(final String operationName, final String idempotencyKey, final int responseStatus) {
        IdempotencyRecord record = repository
                .findByOperationNameAndIdempotencyKey(operationName, idempotencyKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency-Key inexistente"));
        record.setCompleted(Boolean.TRUE);
        record.setResponseStatus(responseStatus);
        record.setUpdatedAt(LocalDateTime.now());
        repository.saveAndFlush(record);
    }

    @Override
    @Transactional
    public void abort(final String operationName, final String idempotencyKey) {
        repository.findByOperationNameAndIdempotencyKey(operationName, idempotencyKey)
                .ifPresent(repository::delete);
    }

    private void validateInputs(
            final String operationName,
            final String idempotencyKey,
            final UUID resourceId,
            final String requestHash) {
        if (isBlank(operationName) || isBlank(idempotencyKey) || resourceId == null || isBlank(requestHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key inválida para a operação");
        }
    }

    private IdempotencyRecord buildPendingRecord(
            final String operationName,
            final String idempotencyKey,
            final UUID resourceId,
            final String requestHash) {
        LocalDateTime now = LocalDateTime.now();
        return IdempotencyRecord.builder()
                .operationName(operationName)
                .idempotencyKey(idempotencyKey)
                .resourceId(resourceId)
                .requestHash(requestHash)
                .completed(Boolean.FALSE)
                .responseStatus(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private IdempotencyResult toResult(
            final IdempotencyRecord record,
            final UUID resourceId,
            final String requestHash) {
        boolean sameRequest = resourceId.equals(record.getResourceId())
                && requestHash.equals(record.getRequestHash());
        if (!sameRequest) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Idempotency-Key já utilizada com payload diferente");
        }

        if (Boolean.TRUE.equals(record.getCompleted())) {
            Integer status = record.getResponseStatus() == null
                    ? DEFAULT_COMPLETED_STATUS
                    : record.getResponseStatus();
            return new IdempotencyResult(IdempotencyState.COMPLETED, status);
        }

        return new IdempotencyResult(IdempotencyState.IN_PROGRESS, null);
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
