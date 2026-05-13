package com.example.backend.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

import com.example.backend.domain.entity.User;

/**
 * Testes para UserRepository.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .email("admin@example.com")
                .name("Admin User")
                .role("ADMIN")
                .pawd("encrypted_password_1")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user2 = User.builder()
                .email("approver@example.com")
                .name("Approver User")
                .role("APPROVER")
                .pawd("encrypted_password_2")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user3 = User.builder()
                .email("viewer@example.com")
                .name("Viewer User")
                .role("VIEWER")
                .pawd("encrypted_password_3")
                .status("INACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);
    }

    @Test
    void testSaveUsuario() {
        User newUser = User.builder()
                .email("newuser@example.com")
                .name("New User")
                .role("USER")
                .pawd("encrypted_password_new")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User saved = repository.save(newUser);

        assertNotNull(saved.getEmail());
        assertEquals("newuser@example.com", saved.getEmail());
        assertEquals("New User", saved.getName());
    }

    @Test
    void testFindByEmailExistente() {
        Optional<User> found = repository.findByEmail("admin@example.com");

        assertTrue(found.isPresent());
        assertEquals("Admin User", found.get().getName());
        assertEquals("ADMIN", found.get().getRole());
    }

    @Test
    void testFindByEmailNaoExistente() {
        Optional<User> found = repository.findByEmail("inexistente@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmailComResultado() {
        boolean exists = repository.existsByEmail("admin@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmailSemResultado() {
        boolean exists = repository.existsByEmail("inexistente@example.com");

        assertFalse(exists);
    }

    @Test
    void testDeleteByEmail() {
        repository.deleteByEmail("admin@example.com");
        entityManager.flush();

        Optional<User> deleted = repository.findByEmail("admin@example.com");
        assertFalse(deleted.isPresent());
    }

    @Test
    void testDeleteByEmailNaoExistente() {
        assertDoesNotThrow(() -> repository.deleteByEmail("inexistente@example.com"));
    }

    @Test
    void testFindByIdExistente() {
        Optional<User> found = repository.findById("admin@example.com");

        assertTrue(found.isPresent());
        assertEquals("Admin User", found.get().getName());
    }

    @Test
    void testFindByIdNaoExistente() {
        Optional<User> found = repository.findById("inexistente@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll() {
        List<User> all = repository.findAll();

        assertEquals(3, all.size());
    }

    @Test
    void testUpdateUsuario() {
        user1.setName("Admin User Updated");
        user1.setUpdatedAt(LocalDateTime.now());

        User updated = repository.save(user1);

        entityManager.flush();
        entityManager.clear();

        Optional<User> retrieved = repository.findById("admin@example.com");
        assertTrue(retrieved.isPresent());
        assertEquals("Admin User Updated", retrieved.get().getName());
    }

    @Test
    void testDeleteUsuario() {
        repository.delete(user1);
        entityManager.flush();

        Optional<User> deleted = repository.findById("admin@example.com");
        assertFalse(deleted.isPresent());
    }

    @Test
    void testFindByEmailCaseSensitive() {
        Optional<User> found = repository.findByEmail("ADMIN@EXAMPLE.COM");

        assertFalse(found.isPresent());
    }
}
