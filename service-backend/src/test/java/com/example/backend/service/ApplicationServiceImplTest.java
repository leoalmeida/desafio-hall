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
import java.util.UUID;

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

    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID APPLICATION_ID_10 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID MISSING_APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID OTHER_APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");

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
            .id(APPLICATION_ID_10)
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://repo")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(applicationRepository.saveAndFlush(any(Application.class))).thenReturn(persisted);

        ApplicationResponseDto response = service.create(dto);

        assertEquals(APPLICATION_ID_10, response.getId());
        assertEquals("app-core", response.getName());
        verify(applicationRepository, times(1)).saveAndFlush(any(Application.class));
    }

    @Test
    void updateDeveFalharQuandoIdInvalido() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder().name("new-name").build();

        assertThrows(IllegalArgumentException.class, () -> service.update(null, dto, true));
    }

    @Test
    void updateDeveFalharQuandoAplicacaoNaoExiste() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder().name("new-name").build();
        when(applicationRepository.findById(MISSING_APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(MISSING_APPLICATION_ID, dto, false));
    }

    @Test
    void updateParcialDeveAlterarSomenteCamposInformados() {
        Application existing = Application.builder()
                .id(APPLICATION_ID)
                .name("old-name")
                .ownerTeam("old-team")
                .repoUrl("https://old")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ApplicationRequestDto partialDto = ApplicationRequestDto.builder().name("new-name").build();

        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(existing));
        when(applicationRepository.saveAndFlush(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto response = service.update(APPLICATION_ID, partialDto, false);

        assertEquals("new-name", response.getName());
        assertEquals("old-team", response.getOwnerTeam());
        assertEquals("https://old", response.getRepoUrl());
    }

    @Test
    void deleteDeveFalharQuandoAplicacaoNaoExiste() {
        when(applicationRepository.findById(OTHER_APPLICATION_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(OTHER_APPLICATION_ID));
    }

    @Test
    void deleteDeveExecutarQuandoAplicacaoExiste() {
        Application existing = Application.builder().id(APPLICATION_ID).name("app").build();
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> service.delete(APPLICATION_ID));

        verify(applicationRepository).deleteById(APPLICATION_ID);
    }
}
