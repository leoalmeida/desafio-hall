package com.example.backend.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.backend.dto.ApplicationRequestDto;

class ApplicationValidatorTest {

    private ApplicationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ApplicationValidator();
    }

    @Test
    void constantesDevemTerValoresCorretos() {
        assertEquals(3, ApplicationValidator.NOME_MIN_LENGTH);
        assertEquals(255, ApplicationValidator.NOME_MAX_LENGTH);
        assertEquals(3, ApplicationValidator.OWNERTEAM_MIN_LENGTH);
        assertEquals(255, ApplicationValidator.OWNERTEAM_MAX_LENGTH);
        assertEquals(3, ApplicationValidator.REPOURL_MIN_LENGTH);
        assertEquals(255, ApplicationValidator.REPOURL_MAX_LENGTH);
        // Comentado - constantes não existem mais
        // assertEquals(255, ApplicationValidator.DESCRICAO_MAX_LENGTH);
        // assertEquals(1, ApplicationValidator.MIN_VALOR_APPLICATION);
    }

    @Test
    void validateDeveRetornarDtoQuandoValido() {
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name("minha-app")
                .ownerTeam("Equipe Dev")
                .repoUrl("https://github.com/example/minha-app")
                .build();

        ApplicationRequestDto result = validator.validate(dto);

        assertNotNull(result);
        assertEquals("minha-app", result.getName());
    }

    @Test
    void validateDeveLancarExcecaoQuandoNomeExcederTamanhoMaximo() {
        String nomeLongo = "A".repeat(ApplicationValidator.NOME_MAX_LENGTH + 1);
        ApplicationRequestDto dto = ApplicationRequestDto.builder()
                .name(nomeLongo)
                .build();

        assertThrows(IllegalArgumentException.class, () -> validator.validate(dto));
    }

    @Test
    void validatorEhSubclasseDeObjectsValidator() {
        assertEquals(true, validator instanceof ObjectsValidator);
    }
}
