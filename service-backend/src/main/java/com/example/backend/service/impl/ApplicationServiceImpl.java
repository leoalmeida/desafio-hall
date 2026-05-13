package com.example.backend.service.impl;

import com.example.backend.domain.entity.Application;
import com.example.backend.domain.repository.ApplicationRepository;
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ApplicationMapper;
import com.example.backend.validator.ObjectsValidator;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.service.ApplicationService;

/**
 * Implementação do serviço responsável pelo gerenciamento de aplicações.
 */
@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository repository;

    private final ObjectsValidator<ApplicationRequestDto> validador;

    @Autowired
    public ApplicationServiceImpl(final ApplicationRepository repository) {
        this.repository = Objects.requireNonNull(repository, "ApplicationRepository não pode ser nulo");
        this.validador = new ObjectsValidator<ApplicationRequestDto>();
    }

    /**
     * Cria uma nova aplicação no sistema.
     * Valida os dados fornecidos antes de persistir no banco de dados.
     *
     * @param dto Dados da aplicação a ser criada
     * @return ApplicationResponseDto com os dados da aplicação criada, incluindo ID gerado
     * @throws IllegalArgumentException se os dados fornecidos forem inválidos
     * @throws BusinessException se ocorrer um erro de negócio ao criar a aplicação
     */
    @Override
    @Transactional
    public ApplicationResponseDto create(@NonNull final ApplicationRequestDto dto)
            throws IllegalArgumentException, BusinessException {
        if (null == dto.getName() || null == dto.getOwnerTeam() || null == dto.getRepoUrl()) {
            throw new IllegalArgumentException("Objeto inválido");
        }
        log.info("Criando nova aplicação: {}", dto.getName());
        // Valida a aplicação e converte antes de salvar
        Application entityIn = ApplicationMapper.mapRequest(validador.validate(dto));
        if (entityIn == null) {
            throw new BusinessException("Erro ao converter dados.");
        }
        log.info("Aplicação mapeada para entidade: {}", entityIn.getName());
        Application entityOut = repository.saveAndFlush(entityIn); // Salva a aplicação no repositório

        return ApplicationMapper.mapResponse(entityOut);
    }

    /**
     * Altera os dados de uma aplicação existente.
     * Valida os dados fornecidos antes de persistir as alterações.
     *
     * @param id Identificador único da aplicação a ser atualizado
     * @param dto Novos dados da aplicação
     * @return ApplicationResponseDto com os dados atualizados
     * @throws EntityNotFoundException se a aplicação não for encontrada
     * @throws IllegalArgumentException se os dados fornecidos forem inválidos
     * @throws BusinessException se ocorrer um erro de negócio ao atualizar a aplicação
     */
    @Override
    @Transactional
    public ApplicationResponseDto update(
            @NonNull final Long id, @NonNull final ApplicationRequestDto dto, @NonNull final Boolean isFullUpdate)
            throws EntityNotFoundException, IllegalArgumentException, BusinessException {
        if (id <= 0) {
            throw new IllegalArgumentException("Identificador inválido");
        }
        log.info("Atualizando dados da aplicação ID={} com os seguintes dados: {}", id, dto.toString());
        // Valida a aplicação antes de salvar
        ApplicationRequestDto validated = isFullUpdate ? validador.validate(dto) : dto;

        if (validated == null) {
            throw new BusinessException("Erro ao validar objeto");
        }

        Application entity =
                repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Aplicação não encontrada"));

        // Faz o merge dos dados validados para a entidade existente
        Application merged = ApplicationMapper.map(entity, validated, isFullUpdate);
        Application saved = repository.saveAndFlush(merged);
        log.info("Aplicação ID={} atualizada com sucesso", id);
        return ApplicationMapper.mapResponse(saved);
    }

    /**
     * Busca uma aplicação específica pelo seu identificador único.
     *
     * @param id Identificador único da aplicação
     * @return ApplicationResponseDto com os dados da aplicação encontrada
     * @throws EntityNotFoundException se a aplicação não for encontrada
     * @throws IllegalArgumentException se o ID fornecido for inválido
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDto findById(@NonNull final Long id)
            throws EntityNotFoundException, IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("Identificador inválido");
        }
        return repository
                .findById(id)
                .map(ApplicationMapper::mapResponse)
                .orElseThrow(() -> new EntityNotFoundException("Aplicação não encontrada"));
    }

    /**
     * Retorna uma lista de todas as aplicações cadastradas no sistema.
     *
     * @return Lista contendo ApplicationResponseDtos de todas as aplicações
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> findAll() {
        return repository.findAll().stream().map(ApplicationMapper::mapResponse).collect(Collectors.toList());
    }

    /**
     * Filtra aplicações pelo nome usando busca parcial.
     * Útil para realizar buscas por nome ou parte do nome da aplicação.
     *
     * @param nome Nome ou parte do nome a ser pesquisado
     * @return Lista de ApplicationResponseDtos que correspondem ao critério de busca
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> filterByName(final String nome) {
        return repository.searchByName(nome).stream()
                .map(ApplicationMapper::mapResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove uma aplicação do sistema.
     * Valida se a aplicação existe antes de proceder com a remoção.
     *
     * @param aplicacaoId Identificador único da aplicação a ser removida
     * @throws BusinessException se a aplicação não for encontrada ou ID for inválido
     * @throws IllegalArgumentException se o ID fornecido for inválido
     * @throws EntityNotFoundException se a aplicação não for encontrada
     */
    @Override
    @Transactional
    public void delete(@NonNull final Long aplicacaoId)
            throws BusinessException, IllegalArgumentException, EntityNotFoundException {
        if (aplicacaoId <= 0) {
            throw new IllegalArgumentException("Identificador inválido para remoção.");
        }

        repository
                .findById(aplicacaoId)
                .map(ApplicationMapper::mapResponse)
                .orElseThrow(() -> new EntityNotFoundException("Aplicação não encontrada"));
        repository.deleteById(aplicacaoId);
        log.info("Aplicação removida: ID={}", aplicacaoId);
    }
}
