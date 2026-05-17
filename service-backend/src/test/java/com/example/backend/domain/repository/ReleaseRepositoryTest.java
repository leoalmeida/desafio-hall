package com.example.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.StatusEnum;

/**
 * Testes para ReleaseRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReleaseRepositoryTest {

    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID APPLICATION_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID APPLICATION_ID_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID MISSING_RELEASE_ID = UUID.fromString("10000000-0000-0000-0000-000000000099");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReleaseRepository repository;

    private Release release1;
    private Release release2;
    private Release release3;
    private Release release4;

    @BeforeEach
    void setUp() {
        release1 = Release.builder()
                .applicationId(APPLICATION_ID)
                .version("1.0.0")
                .env(EnvironmentEnum.DEV)
                .status(StatusEnum.CREATED)
                .evidenceUrl("https://example.com/evidence1")
                .versionRow(1)
                .createdAt(LocalDateTime.now())
                .build();

        release2 = Release.builder()
            .applicationId(APPLICATION_ID)
                .version("1.0.0")
                .env(EnvironmentEnum.PREPROD)
                .status(StatusEnum.APPROVED_PREPROD)
                .evidenceUrl("https://example.com/evidence2")
                .versionRow(2)
                .createdAt(LocalDateTime.now())
                .deployedAt(LocalDateTime.now())
                .build();

        release3 = Release.builder()
            .applicationId(APPLICATION_ID)
                .version("1.0.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.APPROVED_PROD)
                .evidenceUrl("https://example.com/evidence3")
                .versionRow(3)
                .createdAt(LocalDateTime.now())
                .deployedAt(LocalDateTime.now())
                .build();

        release4 = Release.builder()
            .applicationId(APPLICATION_ID_2)
                .version("2.0.0")
                .env(EnvironmentEnum.DEV)
                .status(StatusEnum.CREATED)
                .versionRow(1)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(release1);
        entityManager.persistAndFlush(release2);
        entityManager.persistAndFlush(release3);
        entityManager.persistAndFlush(release4);
    }

    @Test
    void testSaveRelease() {
        Release newRelease = Release.builder()
                .applicationId(APPLICATION_ID_3)
                .version("3.0.0")
                .env(EnvironmentEnum.DEV)
                .status(StatusEnum.CREATED)
                .versionRow(1)
                .createdAt(LocalDateTime.now())
                .build();

        Release saved = repository.save(newRelease);

        assertNotNull(saved.getId());
        assertEquals(APPLICATION_ID_3, saved.getApplicationId());
        assertEquals("3.0.0", saved.getVersion());
    }

    @Test
    void testFindByIdExistente() {
        Optional<Release> found = repository.findById(release1.getId());

        assertTrue(found.isPresent());
        assertEquals(APPLICATION_ID, found.get().getApplicationId());
        assertEquals("1.0.0", found.get().getVersion());
    }

    @Test
    void testFindByIdNaoExistente() {
        Optional<Release> found = repository.findById(MISSING_RELEASE_ID);

        assertFalse(found.isPresent());
    }

    @Test
    void testFindReleaseComCriteriosExatos() {
        List<Release> found = repository.findRelease(APPLICATION_ID, "1.0.0", EnvironmentEnum.DEV, StatusEnum.CREATED);

        assertEquals(1, found.size());
        assertEquals(release1.getId(), found.get(0).getId());
    }

    @Test
    void testFindReleaseComEnvironmentPreprod() {
        List<Release> found = repository.findRelease(
            APPLICATION_ID,
            "1.0.0",
            EnvironmentEnum.PREPROD,
            StatusEnum.APPROVED_PREPROD);

        assertEquals(1, found.size());
        assertEquals(release2.getId(), found.get(0).getId());
    }

    @Test
    void testFindReleaseComEnvironmentProd() {
        List<Release> found = repository.findRelease(
            APPLICATION_ID,
            "1.0.0",
            EnvironmentEnum.PROD,
            StatusEnum.APPROVED_PROD);

        assertEquals(1, found.size());
        assertEquals(release3.getId(), found.get(0).getId());
    }

    @Test
    void testFindReleaseSemResultados() {
        List<Release> found = repository.findRelease(APPLICATION_ID, "9.9.9", EnvironmentEnum.DEV, StatusEnum.CREATED);

        assertTrue(found.isEmpty());
    }

    @Test
    void testFindReleaseComAplicacaoDiferente() {
        List<Release> found = repository.findRelease(APPLICATION_ID_2, "2.0.0", EnvironmentEnum.DEV, StatusEnum.CREATED);

        assertEquals(1, found.size());
        assertEquals(release4.getId(), found.get(0).getId());
    }

    @Test
    void testFindAll() {
        List<Release> all = repository.findAll();

        assertEquals(4, all.size());
    }

    @Test
    void testUpdateRelease() {
        release1.setStatus(StatusEnum.DEPLOYED);
        release1.setDeployedAt(LocalDateTime.now());

        Release updated = repository.save(release1);

        entityManager.flush();
        entityManager.clear();

        Optional<Release> retrieved = repository.findById(release1.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(StatusEnum.DEPLOYED, retrieved.get().getStatus());
        assertNotNull(retrieved.get().getDeployedAt());
    }

    @Test
    void testDeleteRelease() {
        UUID idToDelete = release1.getId();
        repository.delete(release1);
        entityManager.flush();

        Optional<Release> deleted = repository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindReleaseCaseInsensitiveEnvironment() {
        List<Release> found = repository.findRelease(APPLICATION_ID, "1.0.0", EnvironmentEnum.DEV, StatusEnum.CREATED);

        assertEquals(1, found.size());
    }

    @Test
    void testFindReleaseCaseInsensitiveStatus() {
        List<Release> found = repository.findRelease(
            APPLICATION_ID,
            "1.0.0",
            EnvironmentEnum.PREPROD,
            StatusEnum.APPROVED_PREPROD);

        assertEquals(1, found.size());
    }

    @Test
    void testNaoPermiteDuplicidadePorApplicationVersionEnv() {
        Release duplicated = Release.builder()
                .applicationId(release1.getApplicationId())
                .version(release1.getVersion())
                .env(release1.getEnv())
                .status(StatusEnum.CREATED)
                .versionRow(99)
                .createdAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(duplicated));
    }

    @Test
    void testDeveAplicarOptimisticLockingPorVersionRow() {
        UUID releaseId = release1.getId();
        entityManager.clear();

        Release staleSnapshot = repository.findById(releaseId).orElseThrow();
        entityManager.detach(staleSnapshot);

        Release currentSnapshot = repository.findById(releaseId).orElseThrow();
        currentSnapshot.setStatus(StatusEnum.PENDING_PREPROD);
        repository.saveAndFlush(currentSnapshot);

        staleSnapshot.setStatus(StatusEnum.REJECTED);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> repository.saveAndFlush(staleSnapshot));
    }
}
