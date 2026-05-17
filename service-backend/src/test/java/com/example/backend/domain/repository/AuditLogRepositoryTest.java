package com.example.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.example.backend.domain.entity.AuditLog;

/**
 * Testes para AuditLogRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuditLogRepositoryTest {

    private static final UUID MISSING_AUDIT_LOG_ID = UUID.fromString("30000000-0000-0000-0000-000000000999");
    private static final String APPLICATION_ENTITY_ID = "00000000-0000-0000-0000-000000000001";
    private static final String RELEASE_ENTITY_ID = "10000000-0000-0000-0000-000000000100";
    private static final String USER_ENTITY_ID = "40000000-0000-0000-0000-000000000005";
    private static final String NEW_APPLICATION_ENTITY_ID = "00000000-0000-0000-0000-000000000002";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditLogRepository repository;

    private AuditLog log1;
    private AuditLog log2;
    private AuditLog log3;
    private AuditLog log4;
    private AuditLog log5;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        log1 = AuditLog.builder()
                .actor("admin@example.com")
                .action("CREATE")
                .entity("Application")
                .entityId(APPLICATION_ENTITY_ID)
                .payload("{\"name\": \"app-core\"}")
                .timestamp(now)
                .build();

        log2 = AuditLog.builder()
                .actor("approver@example.com")
                .action("APPROVE")
                .entity("Release")
            .entityId(RELEASE_ENTITY_ID)
                .payload("{\"releaseId\": 100, \"status\": \"APPROVED\"}")
                .timestamp(now.minusHours(1))
                .build();

        log3 = AuditLog.builder()
                .actor("admin@example.com")
                .action("UPDATE")
                .entity("Application")
            .entityId(APPLICATION_ENTITY_ID)
                .payload("{\"name\": \"app-core-updated\"}")
                .timestamp(now.minusHours(2))
                .build();

        log4 = AuditLog.builder()
                .actor("viewer@example.com")
                .action("VIEW")
                .entity("Release")
            .entityId(RELEASE_ENTITY_ID)
                .payload("{\"releaseId\": 100}")
                .timestamp(now.minusHours(3))
                .build();

        log5 = AuditLog.builder()
                .actor("admin@example.com")
                .action("DELETE")
                .entity("User")
            .entityId(USER_ENTITY_ID)
                .payload("{\"email\": \"olduser@example.com\"}")
                .timestamp(now.minusHours(4))
                .build();

        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);
        entityManager.persistAndFlush(log4);
        entityManager.persistAndFlush(log5);
    }

    @Test
    void testSaveAuditLog() {
        AuditLog newLog = AuditLog.builder()
                .actor("newuser@example.com")
                .action("CREATE")
                .entity("Application")
                .entityId(NEW_APPLICATION_ENTITY_ID)
                .payload("{\"name\": \"app-new\"}")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog saved = repository.save(newLog);

        assertNotNull(saved.getId());
        assertEquals("newuser@example.com", saved.getActor());
        assertEquals("CREATE", saved.getAction());
    }

    @Test
    void testFindByIdExistente() {
        Optional<AuditLog> found = repository.findById(log1.getId());

        assertTrue(found.isPresent());
        assertEquals("admin@example.com", found.get().getActor());
        assertEquals("CREATE", found.get().getAction());
    }

    @Test
    void testFindByIdNaoExistente() {
        Optional<AuditLog> found = repository.findById(MISSING_AUDIT_LOG_ID);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByActorContainingIgnoreCaseComResultados() {
        List<AuditLog> found = repository.findByActorContainingIgnoreCase("admin");

        assertEquals(3, found.size());
        assertTrue(found.stream().allMatch(l -> l.getActor().toLowerCase().contains("admin")));
    }

    @Test
    void testFindByActorContainingIgnoreCaseSemResultados() {
        List<AuditLog> found = repository.findByActorContainingIgnoreCase("inexistente");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByActorContainingIgnoreCaseUpperCase() {
        List<AuditLog> found = repository.findByActorContainingIgnoreCase("ADMIN");

        assertEquals(3, found.size());
    }

    @Test
    void testFindByActorContainingIgnoreCaseParcial() {
        List<AuditLog> found = repository.findByActorContainingIgnoreCase("example.com");

        assertEquals(5, found.size());
    }

    @Test
    void testFindByActionContainingIgnoreCaseCreate() {
        List<AuditLog> found = repository.findByActionContainingIgnoreCase("CREATE");

        assertEquals(1, found.size());
        assertEquals("CREATE", found.get(0).getAction());
    }

    @Test
    void testFindByActionContainingIgnoreCaseApprove() {
        List<AuditLog> found = repository.findByActionContainingIgnoreCase("APPROVE");

        assertEquals(1, found.size());
        assertEquals(log2.getId(), found.get(0).getId());
    }

    @Test
    void testFindByActionContainingIgnoreCaseLowerCase() {
        List<AuditLog> found = repository.findByActionContainingIgnoreCase("create");

        assertEquals(1, found.size());
    }

    @Test
    void testFindByActionContainingIgnoreCaseSemResultados() {
        List<AuditLog> found = repository.findByActionContainingIgnoreCase("inexistente");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByEntityComResultados() {
        List<AuditLog> found = repository.findByEntity("Application");

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(l -> l.getEntity().equals("Application")));
    }

    @Test
    void testFindByEntityRelease() {
        List<AuditLog> found = repository.findByEntity("Release");

        assertEquals(2, found.size());
    }

    @Test
    void testFindByEntityUser() {
        List<AuditLog> found = repository.findByEntity("User");

        assertEquals(1, found.size());
        assertEquals(log5.getId(), found.get(0).getId());
    }

    @Test
    void testFindByEntitySemResultados() {
        List<AuditLog> found = repository.findByEntity("Inexistente");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByTimestampBetweenComResultados() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(5);
        LocalDateTime endDate = LocalDateTime.now();

        List<AuditLog> found = repository.findByTimestampBetween(startDate, endDate);

        assertEquals(5, found.size());
    }

    @Test
    void testFindByTimestampBetweenRangoMenor() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(2);
        LocalDateTime endDate = LocalDateTime.now();

        List<AuditLog> found = repository.findByTimestampBetween(startDate, endDate);

        assertEquals(2, found.size());
    }

    @Test
    void testFindByTimestampBetweenSemResultados() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(6);
        LocalDateTime endDate = LocalDateTime.now().minusHours(5);

        List<AuditLog> found = repository.findByTimestampBetween(startDate, endDate);

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindAll() {
        List<AuditLog> all = repository.findAll();

        assertEquals(5, all.size());
    }

    @Test
    void testUpdateAuditLog() {
        log1.setAction("UPDATE");
        log1.setPayload("{\"name\": \"app-core-updated\"}");

        AuditLog updated = repository.save(log1);

        entityManager.flush();
        entityManager.clear();

        Optional<AuditLog> retrieved = repository.findById(log1.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("UPDATE", retrieved.get().getAction());
    }

    @Test
    void testDeleteAuditLog() {
        UUID idToDelete = log1.getId();
        repository.delete(log1);
        entityManager.flush();

        Optional<AuditLog> deleted = repository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindByActorContainingIgnoreCaseVazio() {
        List<AuditLog> found = repository.findByActorContainingIgnoreCase("");

        assertEquals(5, found.size());
    }

    @Test
    void testFindByActionContainingIgnoreCaseVazio() {
        List<AuditLog> found = repository.findByActionContainingIgnoreCase("");

        assertEquals(5, found.size());
    }
}
