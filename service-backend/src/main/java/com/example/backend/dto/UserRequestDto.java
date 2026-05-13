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
 * DTO para requisição de usuário.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "UserRequestDto", description = "Dados de requisição de usuário")
@ToString
public class UserRequestDto {
    @Schema(description = "Email do usuário", example = "usuario@example.com")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Email deve ter no máximo 255 caracteres")
    String email;
    @Schema(description = "Nome do usuário", example = "João da Silva")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Nome deve ter no máximo 255 caracteres")
    String name;
    @Schema(description = "Perfil do usuário", example = "ADMIN")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Perfil deve ter no máximo 255 caracteres")
    String role;

    @Schema(description = "Status do usuário", example = "ACTIVE")
    @Size(max = DtoConstants.MAX_TEXT_LENGTH, message = "Status deve ser ACTIVE ou INACTIVE")
    String status;
}
