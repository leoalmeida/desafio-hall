package com.example.backend.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.dto.ApplicationRequestDto;
import com.example.backend.dto.ReleaseRequestDto;

class ObjectsValidatorTest {

    private static final UUID APPLICATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private ObjectsValidator<ApplicationRequestDto> appValidator;
    private ObjectsValidator<ReleaseRequestDto> releaseValidator;

    @BeforeEach
    void setUp() {
        appValidator = new ObjectsValidator<>();
        releaseValidator = new ObjectsValidator<>();
    }

    @Test
    void validateDeveRetornarObjetoQuandoValido() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name("app-core")
                .ownerTeam("Team A")
                .repoUrl("https://repo.example.com")
                .build();

        ApplicationRequestDto result = appValidator.validate(dto);

        assertNotNull(result);
        assertEquals("app-core", result.getName());
    }

    @Test
    void validateDeveLancarIllegalArgumentExceptionQuandoViolacaoDeConstraint() {
        String nomeGigante = "X".repeat(256);
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name(nomeGigante)
                .build();

        assertThrows(IllegalArgumentException.class, () -> appValidator.validate(dto));
    }

    @Test
    void validateDeveLancarIllegalArgumentExceptionComMensagemDeErro() {
        String nomeGigante = "Y".repeat(256);
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name(nomeGigante)
                .build();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> appValidator.validate(dto));

        assertNotNull(ex.getMessage());
        assertEquals(true, ex.getMessage().startsWith("Error occurred:"));
    }

    @Test
    void validateDeveThrowNullPointerExceptionQuandoObjetoNulo() {
        assertThrows(NullPointerException.class, () -> appValidator.validate(null));
    }

    @Test
    void validateDeveRetornarReleaseRequestDtoQuandoValido() {
        ReleaseRequestDto dto = ReleaseRequestDto.builder()
                .applicationId(APPLICATION_ID)
                .version("V1.0")
                .env(EnvironmentEnum.PROD)
                .status(StatusEnum.CREATED)
                .evidenceUrl("https://evidence.example.com")
                .build();

        ReleaseRequestDto result = releaseValidator.validate(dto);

        assertNotNull(result);
        assertEquals("V1.0", result.getVersion());
    }

    @Test
    void validateDeveLancarExcecaoQuandoReleaseRequestDtoInvalido() {
        // applicationId @NotNull, version @NotBlank, env e status @NotNull são obrigatórios
        ReleaseRequestDto dto = ReleaseRequestDto.builder().build();

        assertThrows(IllegalArgumentException.class, () -> releaseValidator.validate(dto));
    }
}
