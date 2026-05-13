package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para resposta de usuário.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "UserResponseDto", description = "Dados de resposta de usuário")
@ToString
public class UserResponseDto {
    
    @Schema(description = "Email do usuário", example = "usuario@example.com")
    String email;
    @Schema(description = "Nome do usuário", example = "João da Silva")
    String name;
    @Schema(description = "Perfil do usuário", example = "ADMIN")
    String role;
    @Schema(description = "Status do usuário", example = "ATIVO")
    String status;
    @Schema(description = "Data de criação do usuário", example = "2023-01-01T00:00:00Z")
    String createdAt;
    @Schema(description = "Data de atualização do usuário", example = "2023-01-01T00:00:00Z")
    String updatedAt;
}
