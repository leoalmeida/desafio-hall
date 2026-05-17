package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.domain.entity.IdempotencyRecord;
import com.example.backend.domain.repository.IdempotencyRecordRepository;
import com.example.backend.service.impl.IdempotencyServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000010");
    private static final UUID OTHER_RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000011");
    private static final UUID IDEMPOTENCY_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
    private static final UUID IDEMPOTENCY_ID_2 = UUID.fromString("40000000-0000-0000-0000-000000000002");

    @Mock
    private IdempotencyRecordRepository repository;

    private IdempotencyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new IdempotencyServiceImpl(repository);
    }

    @Test
    void beginDeveRetornarNewQuandoChaveForNova() {
        when(repository.findByOperationNameAndIdempotencyKey("PROMOTE_RELEASE", "key-1"))
                .thenReturn(Optional.empty());

        IdempotencyResult result = service.begin(
            "PROMOTE_RELEASE",
            "key-1",
            RELEASE_ID,
            "releaseId=" + RELEASE_ID);

        assertNotNull(result);
        assertEquals(IdempotencyState.NEW, result.state());
        verify(repository).saveAndFlush(any(IdempotencyRecord.class));
    }

    @Test
    void beginDeveRetornarCompletedQuandoRegistroJaConcluido() {
        when(repository.findByOperationNameAndIdempotencyKey("PROMOTE_RELEASE", "key-2"))
                .thenReturn(Optional.of(completedRecord()));

        IdempotencyResult result = service.begin(
            "PROMOTE_RELEASE",
            "key-2",
            RELEASE_ID,
            "releaseId=" + RELEASE_ID);

        assertEquals(IdempotencyState.COMPLETED, result.state());
        assertEquals(204, result.responseStatus());
    }

    @Test
    void beginDeveRetornarInProgressQuandoRegistroPendente() {
        when(repository.findByOperationNameAndIdempotencyKey("PROMOTE_RELEASE", "key-3"))
                .thenReturn(Optional.of(pendingRecord()));

        IdempotencyResult result = service.begin(
            "PROMOTE_RELEASE",
            "key-3",
            RELEASE_ID,
            "releaseId=" + RELEASE_ID);

        assertEquals(IdempotencyState.IN_PROGRESS, result.state());
    }

    @Test
    void beginDeveFalharQuandoMesmaChaveForReutilizadaComOutroPayload() {
        when(repository.findByOperationNameAndIdempotencyKey("PROMOTE_RELEASE", "key-4"))
                .thenReturn(Optional.of(completedRecord()));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
            () -> service.begin(
                "PROMOTE_RELEASE",
                "key-4",
                OTHER_RELEASE_ID,
                "releaseId=" + OTHER_RELEASE_ID));

        assertNotNull(ex);
        assertEquals(409, ex.getStatusCode().value());
    }

    private IdempotencyRecord completedRecord() {
        return IdempotencyRecord.builder()
                .id(IDEMPOTENCY_ID)
                .operationName("PROMOTE_RELEASE")
                .idempotencyKey("key-2")
                .resourceId(RELEASE_ID)
                .requestHash("releaseId=" + RELEASE_ID)
                .responseStatus(204)
                .completed(Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private IdempotencyRecord pendingRecord() {
        return IdempotencyRecord.builder()
                .id(IDEMPOTENCY_ID_2)
                .operationName("PROMOTE_RELEASE")
                .idempotencyKey("key-3")
                .resourceId(RELEASE_ID)
                .requestHash("releaseId=" + RELEASE_ID)
                .completed(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
