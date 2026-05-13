package com.example.backend.service;

import java.util.List;

import com.example.backend.dto.UserRequestDto;
import com.example.backend.dto.UserResponseDto;
import com.example.backend.exception.AuthException;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.EntityNotFoundException;

import lombok.NonNull;

/**
 * Interface de serviço para gerenciamento de usuários.
 */
public interface UserService {

    /**
     * Realiza a criação de um usuário.
     *
     * @param request Dados do usuário a serem criados
     */
    public UserResponseDto create(@NonNull UserRequestDto request) throws EntityNotFoundException, AuthException;

    /**
     * Realiza a busca de todos os usuários.
     *
     * @return Lista de usuários encontrados
     */
    public List<UserResponseDto> findAll() throws EntityNotFoundException, AuthException;


    /**
     * Realiza a atualização de um usuário.
     *
     * @param request Dados do usuário a serem atualizados
     */
    public UserResponseDto update(@NonNull UserRequestDto request) throws EntityNotFoundException, AuthException;

    /**
     * Realiza a exclusão de um usuário.
     *
     * @param email Email do usuário a ser excluído
     */
    public void delete(@NonNull String email) throws EntityNotFoundException, BusinessException;

}
