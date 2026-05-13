package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.domain.entity.Application;
import com.example.backend.domain.repository.ApplicationRepository;
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ApplicationResponseDto;
import com.example.backend.service.impl.ApplicationServiceImpl;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    private ApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(applicationRepository);
    }

    @Test
    void createDeveFalharQuandoCamposObrigatoriosAusentes() {
        ApplicationRequestDto dto =
                ApplicationRequestDto.builder().ownerTeam("Team A").repoUrl("https://repo").build();

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void createDeveSalvarQuandoPayloadValido() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://repo")
                .build();

        Application persisted = Application.builder()
                .id(10L)
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://repo")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.saveAndFlush(any(Application.class))).thenReturn(persisted);

        ApplicationResponseDto response = service.create(dto);

        assertEquals(10L, response.getId());
        assertEquals("app-core", response.getName());
        verify(applicationRepository, times(1)).saveAndFlush(any(Application.class));
    }

    @Test
    void updateDeveFalharQuandoIdInvalido() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder().name("new-name").build();

        assertThrows(IllegalArgumentException.class, () -> service.update(0L, dto, true));
    }

    @Test
    void updateDeveFalharQuandoAplicacaoNaoExiste() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder().name("new-name").build();
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(99L, dto, false));
    }

    @Test
    void updateParcialDeveAlterarSomenteCamposInformados() {
        Application existing = Application.builder()
                .id(1L)
                .name("old-name")
                .ownerTeam("old-team")
                .repoUrl("https://old")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ApplicationRequestDto partialDto = ApplicationRequestDto.builder().name("new-name").build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(applicationRepository.saveAndFlush(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto response = service.update(1L, partialDto, false);

        assertEquals("new-name", response.getName());
        assertEquals("old-team", response.getOwnerTeam());
        assertEquals("https://old", response.getRepoUrl());
    }

    @Test
    void deleteDeveFalharQuandoAplicacaoNaoExiste() {
        when(applicationRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(123L));
    }

    @Test
    void deleteDeveExecutarQuandoAplicacaoExiste() {
        Application existing = Application.builder().id(1L).name("app").build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> service.delete(1L));

        verify(applicationRepository).deleteById(1L);
    }
}
