package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.EntityNotFoundException;
import com.example.backend.security.JwtService;
import com.example.backend.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void createDeveFalharQuandoDtoNulo() {
        assertThrows(BusinessException.class, () -> service.create(null));
    }

    @Test
    void createDeveSalvarUsuarioQuandoPayloadValido() throws BusinessException {
        UserRequestDto dto = UserRequestDto.builder()
                .email("user@email.com")
                .name("User")
                .role("ADMIN")
                .status("ATIVO")
                .build();

        User persisted = User.builder()
                .email("user@email.com")
                .name("User")
                .role("ADMIN")
                .status("ATIVO")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        when(userRepository.saveAndFlush(any(User.class))).thenReturn(persisted);

        UserResponseDto response = service.create(dto);

        assertEquals("user@email.com", response.getEmail());
        assertEquals("User", response.getName());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ATIVO", response.getStatus());
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void updateDeveFalharQuandoUsuarioNaoExiste() {
        UserRequestDto dto = UserRequestDto.builder().email("missing@email.com").name("Novo").build();
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(dto));
    }

    @Test
    void updateDeveAtualizarCamposInformados() throws Exception {
        User existing = User.builder()
                .email("user@email.com")
                .name("Old")
                .role("USER")
                .status("INATIVO")
                .createdAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .build();

        UserRequestDto dto = UserRequestDto.builder()
                .email("user@email.com")
                .name("New")
                .role("ADMIN")
                .status("ATIVO")
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(existing));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = service.update(dto);

        assertEquals("New", response.getName());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ATIVO", response.getStatus());
    }

    @Test
    void findAllDeveRetornarUsuariosMapeados() throws BusinessException {
        User u1 = User.builder().email("u1@email.com").name("U1").role("USER").status("ATIVO").build();
        User u2 = User.builder().email("u2@email.com").name("U2").role("ADMIN").status("ATIVO").build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponseDto> response = service.findAll();

        assertEquals(2, response.size());
        assertEquals("u1@email.com", response.get(0).getEmail());
        assertEquals("u2@email.com", response.get(1).getEmail());
    }

    @Test
    void deleteDeveFalharQuandoEmailInvalido() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(" "));
    }

    @Test
    void deleteDeveFalharQuandoUsuarioNaoExiste() {
        when(userRepository.existsByEmail("missing@email.com")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.delete("missing@email.com"));
    }

    @Test
    void deleteDeveRemoverQuandoUsuarioExiste() throws Exception {
        when(userRepository.existsByEmail("user@email.com")).thenReturn(true);

        service.delete("user@email.com");

        verify(userRepository).deleteByEmail("user@email.com");
    }
}
