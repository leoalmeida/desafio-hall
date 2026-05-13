package com.example.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.User;
import com.example.backend.domain.repository.UserRepository;
import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.EntityNotFoundException;
import com.example.backend.security.JwtService;
import com.example.backend.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do serviço de Gerenciamento de Usuários.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(final UserRepository repository, 
                            final JwtService jwtService, 
                            final PasswordEncoder passwordEncoder) {
        this.repository = Objects.requireNonNull(repository, "UserRepository não pode ser nulo");
    }

    @Override
    @Transactional
    public UserResponseDto create(final UserRequestDto dto) throws BusinessException {
        if (dto == null) {
            throw new BusinessException("Dados do usuário não podem ser nulos");
        }
        log.info("Criando novo usuário: {}", dto.getEmail());

        User entity = User.builder()
            .email(dto.getEmail())
            .name(dto.getName())
            .role(dto.getRole())
            .status(dto.getStatus())
            .build();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        User saved = repository.saveAndFlush(entity);
        log.info("Usuário criado com sucesso. Email: {}", saved.getEmail());

        return new UserResponseDto(
            saved.getEmail(),
            saved.getName(),
            saved.getRole(),
            saved.getStatus(),
            saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : "",
            saved.getUpdatedAt() != null ? saved.getUpdatedAt().toString() : ""
        );
    }

    @Override
    @Transactional
    public UserResponseDto update(final UserRequestDto dto) throws EntityNotFoundException, BusinessException {
        if (dto == null) {
            throw new BusinessException("Dados do usuário não podem ser nulos");
        }
        User entity = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(dto.getEmail(),
                        "Usuário não encontrado com email: " + dto.getEmail()));
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getRole() != null) {
            entity.setRole(dto.getRole());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        User updated = repository.saveAndFlush(entity);
        return new UserResponseDto(
            updated.getEmail(),
            updated.getName(),
            updated.getRole(),
            updated.getStatus(),
            updated.getCreatedAt() != null ? updated.getCreatedAt().toString() : "",
            updated.getUpdatedAt() != null ? updated.getUpdatedAt().toString() : ""
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> findAll() throws BusinessException {
        log.info("Buscando todos os usuários");

        List<User> entities = repository.findAll();

        return entities.stream().map(
            e -> new UserResponseDto(
                e.getEmail(),
                e.getName(),
                e.getRole(),
                e.getStatus(),
                e.getCreatedAt() != null ? e.getCreatedAt().toString() : "",
                e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : ""
            )
        ).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void delete(final String email) throws EntityNotFoundException, BusinessException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do usuário inválido");
        }
        log.info("Removendo usuário email: {}", email);

        if (!repository.existsByEmail(email)) {
            throw new EntityNotFoundException(email, "Usuário não encontrado com email: " + email);
        }

        repository.deleteByEmail(email);
        log.info("Usuário removido com sucesso. Email: {}", email);
    }

}
