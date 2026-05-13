package com.example.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.example.backend.domain.entity.Application;

/**
 * Testes para ApplicationRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApplicationRepository repository;

    private Application app1;
    private Application app2;
    private Application app3;

    @BeforeEach
    void setUp() {
        app1 = Application.builder()
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://github.com/team-a/core")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        app2 = Application.builder()
                .name("app-gateway")
                .ownerTeam("Team B")
                .repoUrl("https://github.com/team-b/gateway")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        app3 = Application.builder()
                .name("app-reporting")
                .ownerTeam("Team C")
                .repoUrl("https://github.com/team-c/reporting")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(app1);
        entityManager.persistAndFlush(app2);
        entityManager.persistAndFlush(app3);
    }

    @Test
    void testSaveAplicacao() {
        Application newApp = Application.builder()
                .name("app-new")
                .ownerTeam("Team D")
                .repoUrl("https://github.com/team-d/new")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Application saved = repository.save(newApp);

        assertNotNull(saved.getId());
        assertEquals("app-new", saved.getName());
        assertEquals("Team D", saved.getOwnerTeam());
    }

    @Test
    void testFindByIdExistente() {
        Optional<Application> found = repository.findById(app1.getId());

        assertTrue(found.isPresent());
        assertEquals("app-core", found.get().getName());
        assertEquals("Team A", found.get().getOwnerTeam());
    }

    @Test
    void testFindByIdNaoExistente() {
        Optional<Application> found = repository.findById(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testSearchByNameComResultados() {
        List<Application> results = repository.searchByName("app");

        assertEquals(3, results.size());
    }

    @Test
    void testSearchByNameCaseSensitive() {
        List<Application> results = repository.searchByName("APP");

        assertEquals(3, results.size());
    }

    @Test
    void testSearchByNameParcial() {
        List<Application> results = repository.searchByName("core");

        assertEquals(1, results.size());
        assertEquals("app-core", results.get(0).getName());
    }

    @Test
    void testSearchByNameSemResultados() {
        List<Application> results = repository.searchByName("inexistente");

        assertTrue(results.isEmpty());
    }

    @Test
    void testUpdateAplicacao() {
        app1.setName("app-core-updated");
        app1.setUpdatedAt(LocalDateTime.now());

        Application updated = repository.save(app1);

        entityManager.flush();
        entityManager.clear();

        Optional<Application> retrieved = repository.findById(app1.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("app-core-updated", retrieved.get().getName());
    }

    @Test
    void testDeleteAplicacao() {
        Long idToDelete = app1.getId();
        repository.delete(app1);
        entityManager.flush();

        Optional<Application> deleted = repository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindAll() {
        List<Application> all = repository.findAll();

        assertEquals(3, all.size());
    }

    @Test
    void testFindByIdForUpdateDeveRetornarPessimisticLock() {
        Optional<Application> found = repository.findByIdForUpdate(app1.getId());

        assertTrue(found.isPresent());
        assertEquals("app-core", found.get().getName());
    }

    @Test
    void testSearchByNameVazio() {
        List<Application> results = repository.searchByName("");

        assertEquals(3, results.size());
    }
}
