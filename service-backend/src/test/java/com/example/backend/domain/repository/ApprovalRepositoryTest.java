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

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.OutcomeEnum;

/**
 * Testes para ApprovalRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ApprovalRepositoryTest {

    private static final UUID RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID RELEASE_ID_2 = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID RELEASE_ID_3 = UUID.fromString("10000000-0000-0000-0000-000000000003");
    private static final UUID RELEASE_ID_4 = UUID.fromString("10000000-0000-0000-0000-000000000004");
    private static final UUID MISSING_APPROVAL_ID = UUID.fromString("20000000-0000-0000-0000-000000000999");
    private static final UUID MISSING_RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000999");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApprovalRepository repository;

    private Approval approval1;
    private Approval approval2;
    private Approval approval3;
    private Approval approval4;

    @BeforeEach
    void setUp() {
        approval1 = Approval.builder()
                .releaseId(RELEASE_ID)
                .approverEmail("approver1@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Approved for production")
                .timestamp(LocalDateTime.now())
                .build();

        approval2 = Approval.builder()
            .releaseId(RELEASE_ID)
                .approverEmail("approver2@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Approved after review")
                .timestamp(LocalDateTime.now())
                .build();

        approval3 = Approval.builder()
            .releaseId(RELEASE_ID_2)
                .approverEmail("approver1@example.com")
                .outcome(OutcomeEnum.REJECTED)
                .notes("Needs more testing")
                .timestamp(LocalDateTime.now())
                .build();

        approval4 = Approval.builder()
            .releaseId(RELEASE_ID_3)
                .approverEmail("approver3@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .timestamp(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(approval1);
        entityManager.persistAndFlush(approval2);
        entityManager.persistAndFlush(approval3);
        entityManager.persistAndFlush(approval4);
    }

    @Test
    void testSaveApproval() {
        Approval newApproval = Approval.builder()
                .releaseId(RELEASE_ID_4)
                .approverEmail("approver4@example.com")
                .outcome(OutcomeEnum.APPROVED)
                .notes("Approved")
                .timestamp(LocalDateTime.now())
                .build();

        Approval saved = repository.save(newApproval);

        assertNotNull(saved.getId());
        assertEquals(RELEASE_ID_4, saved.getReleaseId());
        assertEquals("approver4@example.com", saved.getApproverEmail());
    }

    @Test
    void testFindByIdExistente() {
        Optional<Approval> found = repository.findById(approval1.getId());

        assertTrue(found.isPresent());
        assertEquals(RELEASE_ID, found.get().getReleaseId());
        assertEquals("approver1@example.com", found.get().getApproverEmail());
    }

    @Test
    void testFindByIdNaoExistente() {
        Optional<Approval> found = repository.findById(MISSING_APPROVAL_ID);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByReleaseIdComResultados() {
        List<Approval> found = repository.findByReleaseId(RELEASE_ID);

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(a -> a.getReleaseId().equals(RELEASE_ID)));
    }

    @Test
    void testFindByReleaseIdSemResultados() {
        List<Approval> found = repository.findByReleaseId(MISSING_RELEASE_ID);

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByReleaseIdComUmaAprovacao() {
        List<Approval> found = repository.findByReleaseId(RELEASE_ID_3);

        assertEquals(1, found.size());
        assertEquals(approval4.getId(), found.get(0).getId());
    }

    @Test
    void testFindByApproverEmailComResultados() {
        List<Approval> found = repository.findByApproverEmail("approver1@example.com");

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(a -> a.getApproverEmail().equalsIgnoreCase("approver1@example.com")));
    }

    @Test
    void testFindByApproverEmailSemResultados() {
        List<Approval> found = repository.findByApproverEmail("inexistente@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByApproverEmailCaseInsensitive() {
        List<Approval> found = repository.findByApproverEmail("APPROVER1@EXAMPLE.COM");

        assertEquals(2, found.size());
    }

    @Test
    void testFindByOutcomeApproved() {
        List<Approval> found = repository.findByOutcome(OutcomeEnum.APPROVED);

        assertEquals(3, found.size());
        assertTrue(found.stream().allMatch(a -> a.getOutcome().equals(OutcomeEnum.APPROVED)));
    }

    @Test
    void testFindByOutcomeRejected() {
        List<Approval> found = repository.findByOutcome(OutcomeEnum.REJECTED);

        assertEquals(1, found.size());
        assertEquals(approval3.getId(), found.get(0).getId());
    }

    @Test
    void testFindAll() {
        List<Approval> all = repository.findAll();

        assertEquals(4, all.size());
    }

    @Test
    void testUpdateApproval() {
        approval1.setOutcome(OutcomeEnum.REJECTED);
        approval1.setNotes("Actually rejected");

        Approval updated = repository.save(approval1);

        entityManager.flush();
        entityManager.clear();

        Optional<Approval> retrieved = repository.findById(approval1.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(OutcomeEnum.REJECTED, retrieved.get().getOutcome());
        assertEquals("Actually rejected", retrieved.get().getNotes());
    }

    @Test
    void testDeleteApproval() {
        UUID idToDelete = approval1.getId();
        repository.delete(approval1);
        entityManager.flush();

        Optional<Approval> deleted = repository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindByApproverEmailUnicoResultado() {
        List<Approval> found = repository.findByApproverEmail("approver3@example.com");

        assertEquals(1, found.size());
        assertEquals(approval4.getId(), found.get(0).getId());
    }
}
