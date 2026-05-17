package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.dto.AuthRequestDto;
import com.example.backend.dto.AuthResponseDto;
import com.example.backend.security.JwtService;
import com.example.backend.service.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void loginUserDeveFalharQuandoRequestNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.loginUser(null));
    }

    @Test
    void loginUserDeveRetornarUnauthorizedQuandoUsuarioNaoExiste() {
        AuthRequestDto request = AuthRequestDto.builder().email("user@email.com").pawd("123").build();
        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.loginUser(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void loginUserDeveAutenticarUsuarioComHashBcrypt() {
        AuthRequestDto request = AuthRequestDto.builder().email("user@email.com").pawd("123456").build();
        User user = User.builder()
                .email("user@email.com")
                .name("User")
                .role("admin")
                .pawd("$2a$10$hash")
                .status("ativo")
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "$2a$10$hash")).thenReturn(true);
        when(jwtService.generateToken("user@email.com", "User", "ADMIN")).thenReturn("jwt-token");

        AuthResponseDto response = service.loginUser(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUserDeveMigrarSenhaLegadaQuandoValida() {
        AuthRequestDto request = AuthRequestDto.builder().email("legacy@email.com").pawd("123456").build();
        User user = User.builder()
                .email("legacy@email.com")
                .name("Legacy")
                .role(null)
                .pawd("123456")
                .status("ativo")
                .build();

        when(userRepository.findByEmail("legacy@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123456")).thenReturn("$2a$newhash");
        when(jwtService.generateToken("legacy@email.com", "Legacy", "VIEWER")).thenReturn("jwt-viewer");

        AuthResponseDto response = service.loginUser(request);

        assertEquals("jwt-viewer", response.getToken());
        verify(userRepository).save(eq(user));
        assertEquals("$2a$newhash", user.getPawd());
    }

    @Test
    void loginUserDeveFalharQuandoSenhaInvalida() {
        AuthRequestDto request = AuthRequestDto.builder().email("user@email.com").pawd("errada").build();
        User user = User.builder()
                .email("user@email.com")
                .name("User")
                .role("ADMIN")
                .pawd("$2a$10$hash")
                .status("ativo")
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "$2a$10$hash")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.loginUser(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void loginUserDeveFalharQuandoUsuarioInativo() {
        AuthRequestDto request = AuthRequestDto.builder().email("user@email.com").pawd("123456").build();
        User user = User.builder()
                .email("user@email.com")
                .name("User")
                .role("ADMIN")
                .pawd("$2a$10$hash")
                .status("inativo")
                .build();

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "$2a$10$hash")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.loginUser(request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
}
