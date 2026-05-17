package com.example.backend.service;

import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import com.example.backend.exception.BusinessException;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;

public interface ApplicationService {

    List<ApplicationResponseDto> findAll() throws BusinessException;

        ApplicationResponseDto findById(@NonNull UUID id)
            throws EntityNotFoundException, IllegalArgumentException, BusinessException;

    ApplicationResponseDto create(@NonNull ApplicationRequestDto application)
            throws IllegalArgumentException, BusinessException;

    ApplicationResponseDto update(
            @NonNull UUID id, @NonNull ApplicationRequestDto application, @NonNull Boolean isFullUpdate)
            throws EntityNotFoundException, IllegalArgumentException, BusinessException;

    void delete(@NonNull UUID id) throws EntityNotFoundException, IllegalArgumentException, BusinessException;

    List<ApplicationResponseDto> filterByName(String name) throws BusinessException;
}
