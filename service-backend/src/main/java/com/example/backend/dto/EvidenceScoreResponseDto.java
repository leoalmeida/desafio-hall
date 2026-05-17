package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO para resposta de score de evidência de release.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "EvidenceScoreResponseDto", description = "Resultado de score de evidência de release")
@ToString
public class EvidenceScoreResponseDto {

    @Schema(description = "ID da release", example = "1")
    private Long releaseId;

    @Schema(description = "Score de evidência determinístico (0..100)", example = "85")
    private Integer score;

        @Schema(
            description = "Evidência informada na release",
            example = "https://ci.example.com/reports/rel-1?result=PASS")
    private String evidenceUrl;

        @Schema(
            description = "Resumo das regras aplicadas",
            example = "validUrl=true, reportPattern=true, passToken=true, statusWeight=10, envWeight=10")
    private String rationale;
}
