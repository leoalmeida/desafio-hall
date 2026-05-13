package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para resposta de autenticação.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "AuthResponseDto", description = "Dados de autenticação do usuário")
@ToString
public class AuthResponseDto {
    @Schema(description = "Token de autenticação do usuário", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token;
    @Schema(description = "Tipo do token", example = "Bearer")
    String tokenType;
}
