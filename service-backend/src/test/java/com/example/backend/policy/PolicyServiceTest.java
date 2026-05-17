package com.example.backend.policy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;

class PolicyServiceTest {

    @Test
    void getCurrentPolicyDeveCarregarArquivoExternoQuandoConfigurado() throws IOException {
        Path tempFile = Files.createTempFile("policy-service", ".json");
        String json = """
                {
                  "minApprovals": 3,
                  "minScore": 80,
                  "freezeWindows": [],
                  "approvalRoles": ["MANAGER"]
                }
                """;
        Files.writeString(tempFile, json, StandardCharsets.UTF_8);

        PolicyService service = new PolicyService(new ObjectMapper(), tempFile.toString());

        PolicyConfig config = service.getCurrentPolicy();

        assertEquals(3, config.getMinApprovals());
        assertEquals(80, config.getMinScore());
    }

    @Test
    void validateApprovalThresholdsDeveFalharQuandoAbaixoDoMinApprovals() {
        PolicyService service = serviceFromJson("""
                {
                  "minApprovals": 2,
                  "minScore": 10,
                  "freezeWindows": []
                }
                """);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateApprovalThresholds(1, 1));
        assertNotNull(ex);
    }

    @Test
    void validateApprovalThresholdsDeveFalharQuandoScoreAbaixoDoMinimo() {
        PolicyService service = serviceFromJson("""
                {
                  "minApprovals": 1,
                  "minScore": 80,
                  "freezeWindows": []
                }
                """);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateApprovalThresholds(1, 2));
        assertNotNull(ex);
    }

    @Test
    void validateApprovalThresholdsDevePassarQuandoRegrasAtendidas() {
        PolicyService service = serviceFromJson("""
                {
                  "minApprovals": 1,
                  "minScore": 50,
                  "freezeWindows": []
                }
                """);

        assertDoesNotThrow(() -> service.validateApprovalThresholds(1, 2));
    }

    @Test
    void validateFreezeWindowDeveBloquearQuandoJanelaAtivaParaAmbiente() {
        PolicyService service = serviceFromJson("""
                {
                  "minApprovals": 1,
                  "minScore": 0,
                  "freezeWindows": [
                    {
                      "env": "PROD",
                      "start": "00:00",
                      "end": "23:59",
                      "timezone": "UTC"
                    }
                  ]
                }
                """);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.validateFreezeWindow(EnvironmentEnum.PROD));
        assertNotNull(ex);
    }

    @Test
    void validateFreezeWindowDevePermitirQuandoJanelaNaoAfetaAmbiente() {
        PolicyService service = serviceFromJson("""
                {
                  "minApprovals": 1,
                  "minScore": 0,
                  "freezeWindows": [
                    {
                      "env": "PROD",
                      "start": "00:00",
                      "end": "23:59",
                      "timezone": "UTC"
                    }
                  ]
                }
                """);

        assertDoesNotThrow(() -> service.validateFreezeWindow(EnvironmentEnum.DEV));
    }

    private PolicyService serviceFromJson(final String json) {
        try {
            Path tempFile = Files.createTempFile("policy-test", ".json");
            Files.writeString(tempFile, json, StandardCharsets.UTF_8);
            return new PolicyService(new ObjectMapper(), tempFile.toString());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
