package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.impl.UserServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Testes para UserServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplTest")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "user@example.com";
        
        user = User.builder()
                .email(userEmail)
                .pawd("hashed_password")
                .name("Test User")
                .role("ADMIN")
                .status("ACTIVE")
                .build();

        userRequestDto = UserRequestDto.builder()
                .email(userEmail)
                .name("Test User")
                .role("ADMIN")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("create deve criar um novo usuário com sucesso")
    void testCreateUserSuccess() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.create(userRequestDto);

        assertNotNull(result);
        assertEquals(userEmail, result.getEmail());
        assertEquals("Test User", result.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("create com dados inválidos deve lançar IllegalArgumentException")
    void testCreateUserWithInvalidData() {
        UserRequestDto invalidDto = UserRequestDto.builder()
                .email(null)
                .password("password")
                .name("Test")
                .role("USER")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.create(invalidDto));
    }

    @Test
    @DisplayName("findAll deve retornar lista de usuários")
    void testFindAllUsers() {
        User user2 = User.builder()
                .email("user2@example.com")
                .password("hashed_password2")
                .name("Test User 2")
                .role("USER")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserResponseDto> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findAll com lista vazia deve retornar lista vazia")
    void testFindAllUsersEmpty() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        List<UserResponseDto> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("update deve atualizar um usuário existente")
    void testUpdateUserSuccess() {
        User updatedUser = User.builder()
                .email(userEmail)
                .password("new_password")
                .name("Updated Name")
                .role("USER")
                .build();

        UserRequestDto updateDto = UserRequestDto.builder()
                .email(userEmail)
                .password("new_password")
                .name("Updated Name")
                .role("USER")
                .build();

        when(userRepository.findById(userEmail)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponseDto result = userService.update(updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(userRepository, times(1)).findById(userEmail);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("update com usuário não existente deve lançar EntityNotFoundException")
    void testUpdateUserNotFound() {
        UserRequestDto updateDto = UserRequestDto.builder()
                .email("nonexistent@example.com")
                .password("password")
                .name("User")
                .role("USER")
                .build();

        when(userRepository.findById("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.update(updateDto));
    }

    @Test
    @DisplayName("delete deve remover um usuário existente")
    void testDeleteUserSuccess() {
        when(userRepository.existsById(userEmail)).thenReturn(true);

        userService.delete(userEmail);

        verify(userRepository, times(1)).deleteById(userEmail);
    }

    @Test
    @DisplayName("delete com usuário não existente deve lançar EntityNotFoundException")
    void testDeleteUserNotFound() {
        when(userRepository.existsById("nonexistent@example.com")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.delete("nonexistent@example.com"));
    }

    @Test
    @DisplayName("create com null deve lançar IllegalArgumentException")
    void testCreateUserWithNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.create(null));
    }

    @Test
    @DisplayName("findAll deve chamar repository exatamente uma vez")
    void testFindAllCallsRepositoryOnce() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        userService.findAll();

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }
}
