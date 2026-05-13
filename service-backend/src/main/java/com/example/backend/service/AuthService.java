package com.example.backend.service;

import com.example.backend.dto.AuthRequestDto;
import com.example.backend.dto.AuthResponseDto;
import com.example.backend.exception.AuthException;
import com.example.backend.exception.EntityNotFoundException;

import lombok.NonNull;

/**
 * Interface de serviço para autenticação de usuários.
 */
public interface AuthService {

    /**
     * Realiza o login de um usuário.
     *
     * @param request Dados de autenticação do usuário
     */
    AuthResponseDto loginUser(
            @NonNull AuthRequestDto request)
            throws EntityNotFoundException, AuthException;
}
