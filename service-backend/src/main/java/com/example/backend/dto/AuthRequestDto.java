package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para requisição de autenticação.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "AuthRequestDto", description = "Dados de requisição de autenticação do usuário")
@ToString
public class AuthRequestDto {
    @Schema(description = "Email do usuário", example = "usuario@example.com")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Email deve ter no máximo 255 caracteres")
    String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Senha deve ter no máximo 255 caracteres")
    String pawd;
    
}
