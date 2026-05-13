package com.example.backend.service.impl;

import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.dto.AuthRequestDto;
import com.example.backend.dto.AuthResponseDto;
import com.example.backend.exception.AuthException;
import com.example.backend.security.JwtService;
import com.example.backend.service.AuthService;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de Autenticação.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(final UserRepository repository, 
                            final JwtService jwtService, 
                            final PasswordEncoder passwordEncoder) {
        this.repository = Objects.requireNonNull(repository, "UserRepository não pode ser nulo");
        this.jwtService = Objects.requireNonNull(jwtService, "JwtService não pode ser nulo");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoder não pode ser nulo");
    }

    @Override
    @Transactional
    public AuthResponseDto loginUser(
            @NonNull final AuthRequestDto request)
            throws EntityNotFoundException, AuthException {
        if (request == null) {
            throw new IllegalArgumentException("Dados de autenticação não podem ser nulos");
        }
        log.info("Autenticando usuário: {}", request.getEmail());

        User user = repository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

        validateCredentials(request, user);
        validateActiveStatus(user);

        String role = user.getRole() == null ? "VIEWER" : user.getRole().toUpperCase(Locale.ROOT);
        String token = jwtService.generateToken(user.getEmail(), user.getName(), role);

        log.info("User ID: {} logado com sucesso", request.getEmail());
        return new AuthResponseDto(token,"Bearer");
    }

    private void validateCredentials(final AuthRequestDto request, final User user) {
        String rawPawd = request.getPawd();
        String storedPawd = user.getPawd() == null ? "" : user.getPawd();

        if (storedPawd.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        if (isBcryptHash(storedPawd)) {
            if (!passwordEncoder.matches(rawPawd, storedPawd)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
            }
            return;
        }

        if (!storedPawd.equals(rawPawd)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        // Migra senha legada em texto puro para hash BCrypt no primeiro login valido.
        user.setPawd(passwordEncoder.encode(rawPawd));
        repository.save(user);
    }

    private static boolean isBcryptHash(final String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private void validateActiveStatus(final User user) {
        String status = user.getStatus() == null ? "" : user.getStatus().toLowerCase(Locale.ROOT);
        if (!"ativo".equals(status)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inativo");
        }
    }

}
