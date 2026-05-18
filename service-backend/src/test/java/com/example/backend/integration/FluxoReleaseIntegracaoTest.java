package com.example.backend.integration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.example.backend.domain.entity.Application;
import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.OutcomeEnum;
import com.example.backend.domain.entity.StatusEnum;
import com.example.backend.domain.repository.ApplicationRepository;
import com.example.backend.domain.repository.ApprovalRepository;
import com.example.backend.dto.ReleaseEvidenceUpdateRequestDto;
import com.example.backend.dto.ReleaseRequestDto;
import com.example.backend.dto.ReleaseResponseDto;
import com.example.backend.exception.BusinessException;
import com.example.backend.service.ReleaseService;

/**
 * Testes de integração cobrindo o fluxo completo de release:
 * criar → promover DEV→PREPROD → aprovar → promover PREPROD→PROD,
 * incluindo bloqueio por policy-as-code.
 */
@SpringBootTest
@ActiveProfiles("integrado")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FluxoReleaseIntegracaoTest {

    @Autowired
    private ReleaseService releaseService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    private UUID criarAplicacao(final String nome) {
        Application app = Application.builder()
                .name(nome)
                .ownerTeam("Time Integração")
                .repoUrl("https://github.com/example/repo")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return applicationRepository.saveAndFlush(app).getId();
    }

    private ReleaseResponseDto criarRelease(final UUID appId, final String versao)
            throws BusinessException {
        ReleaseRequestDto dto = ReleaseRequestDto.builder()
                .applicationId(appId)
                .version(versao)
                .env(EnvironmentEnum.DEV)
                .status(StatusEnum.CREATED)
                .build();
        return releaseService.create(dto);
    }

    /**
     * Fluxo happy-path: criar → promover DEV→PREPROD → atualizar evidence URL
     * → aprovar (PREPROD) → promover PREPROD→PROD.
     * Verifica que a release chega ao ambiente PROD com status APPROVED_PROD.
     */
    @Test
    void fluxoCompletoHappyPath() throws BusinessException {
        UUID appId = criarAplicacao("App-HappyPath-" + UUID.randomUUID());

        // 1. Criar release em DEV
        ReleaseResponseDto criada = criarRelease(appId, "1.0.0");
        UUID releaseId = criada.getId();
        assertNotNull(releaseId);
        assertEquals("DEV", criada.getEnv());
        assertEquals("CREATED", criada.getStatus());

        // 2. Promover DEV → PREPROD
        releaseService.promoteRelease(releaseId);
        ReleaseResponseDto emPreprod = releaseService.findById(releaseId);
        assertEquals("PREPROD", emPreprod.getEnv());
        assertEquals("PENDING_PREPROD", emPreprod.getStatus());

        // 3. Atualizar evidence URL (obrigatória para promoção PREPROD→PROD)
        ReleaseEvidenceUpdateRequestDto evidenceDto = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl("https://ci.example.com/reports/rel-1?result=PASS")
                .build();
        releaseService.updateEvidenceUrl(releaseId, evidenceDto);

        // 4. Aprovar release (PREPROD)
        releaseService.approveRelease(releaseId, OutcomeEnum.APPROVED, "aprovador@example.com", "LGTM");
        ReleaseResponseDto aprovada = releaseService.findById(releaseId);
        assertEquals("APPROVED_PREPROD", aprovada.getStatus());

        // 5. Promover PREPROD → PROD
        releaseService.promoteRelease(releaseId);
        ReleaseResponseDto emProd = releaseService.findById(releaseId);
        assertEquals("PROD", emProd.getEnv());
        assertEquals("APPROVED_PROD", emProd.getStatus());
    }

    /**
     * Bloqueio por policy-as-code: uma rejeição anterior reduz o score de
     * aprovações abaixo do minScore configurado (70%), impedindo uma nova
     * aprovação conforme as regras de política vigentes.
     *
     * <p>Fluxo: criar → promover DEV→PREPROD → injetar rejeição anterior →
     * tentar aprovar → BusinessException com mensagem de policy violada.
     */
    @Test
    void aprovacaoBloqueadaPorPolicyScoreInsuficiente() throws BusinessException {
        UUID appId = criarAplicacao("App-PolicyBlock-" + UUID.randomUUID());

        // 1. Criar release em DEV e promover para PREPROD
        ReleaseResponseDto criada = criarRelease(appId, "2.0.0");
        UUID releaseId = criada.getId();
        releaseService.promoteRelease(releaseId);

        // 2. Atualizar evidence URL
        ReleaseEvidenceUpdateRequestDto evidenceDto = ReleaseEvidenceUpdateRequestDto.builder()
                .evidenceUrl("https://ci.example.com/report")
                .build();
        releaseService.updateEvidenceUrl(releaseId, evidenceDto);

        // 3. Injetar uma aprovação rejeitada diretamente no repositório,
        //    simulando uma rejeição anterior registrada por outro revisor.
        //    Com 1 rejeição existente, ao tentar aprovar:
        //    approvedCount = 0 + 1 = 1, totalCount = 1 + 1 = 2
        //    score = 1 * 100 / 2 = 50 < minScore(70) → bloqueado por policy.
        approvalRepository.saveAndFlush(Approval.builder()
                .releaseId(releaseId)
                .approverEmail("revisor-anterior@example.com")
                .outcome(OutcomeEnum.REJECTED)
                .notes("Rejeição anterior")
                .timestamp(LocalDateTime.now().minusMinutes(10))
                .build());

        // 4. Tentar aprovar — policy deve bloquear por score insuficiente
        BusinessException ex = assertThrows(BusinessException.class, () ->
                releaseService.approveRelease(
                        releaseId,
                        OutcomeEnum.APPROVED,
                        "novo-aprovador@example.com",
                        "Tentando aprovar"));

        assertTrue(ex.getMessage().contains("Policy-as-code violada"),
                "Mensagem deve indicar violação de policy, mas foi: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("minScore"),
                "Mensagem deve mencionar minScore, mas foi: " + ex.getMessage());
    }
}
