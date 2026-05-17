package com.example.backend.policy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PolicyService {

    private static final String DEFAULT_TIMEZONE = "UTC";
    private static final int FULL_SCORE = 100;

    private final ObjectMapper objectMapper;
    private final String policyFilePath;

    private volatile PolicyConfig cachedPolicy;
    private volatile long cachedModifiedAt = -1L;

    public PolicyService(
            final ObjectMapper objectMapper,
            @Value("${policy.file.path:}") final String policyFilePath) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper não pode ser nulo");
        this.policyFilePath = policyFilePath == null ? "" : policyFilePath.trim();
    }

    public PolicyConfig getCurrentPolicy() {
        try {
            return loadPolicyIfNeeded();
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível carregar policy.json em runtime", ex);
        }
    }

    public void validateFreezeWindow(final EnvironmentEnum env) throws BusinessException {
        PolicyConfig policy = getCurrentPolicy();
        List<FreezeWindow> windows = policy.getFreezeWindows();

        if (windows == null || windows.isEmpty()) {
            return;
        }

        for (FreezeWindow window : windows) {
            if (window == null || window.getEnv() == null) {
                continue;
            }

            String configuredEnv = window.getEnv().trim().toUpperCase(Locale.ROOT);
            if (!configuredEnv.equals(env.name())) {
                continue;
            }

            if (isInsideWindow(window)) {
                throw new BusinessException(
                        String.format(
                                "Janela de freeze ativa para ambiente %s. Mudança bloqueada por policy-as-code.",
                                env));
            }
        }
    }

    public void validateApprovalThresholds(final long approvedCount, final long totalCount) throws BusinessException {
        PolicyConfig policy = getCurrentPolicy();

        int minApprovals = Math.max(0, policy.getMinApprovals());
        int minScore = Math.max(0, policy.getMinScore());

        if (approvedCount < minApprovals) {
            throw new BusinessException(
                    String.format(
                            "Policy-as-code violada: minApprovals=%d, aprovações válidas=%d",
                            minApprovals,
                            approvedCount));
        }

        int score = calculateScore(approvedCount, totalCount);
        if (score < minScore) {
            throw new BusinessException(
                    String.format(
                            "Policy-as-code violada: minScore=%d, score atual=%d",
                            minScore,
                            score));
        }
    }

    private int calculateScore(final long approvedCount, final long totalCount) {
        if (totalCount <= 0) {
            return approvedCount > 0 ? FULL_SCORE : 0;
        }

        return (int) Math.round((approvedCount * (double) FULL_SCORE) / totalCount);
    }

    private boolean isInsideWindow(final FreezeWindow window) {
        String timezone = window.getTimezone() == null || window.getTimezone().isBlank()
                ? DEFAULT_TIMEZONE
                : window.getTimezone().trim();
        ZoneId zoneId = ZoneId.of(timezone);

        LocalTime start = LocalTime.parse(window.getStart());
        LocalTime end = LocalTime.parse(window.getEnd());
        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime();

        if (!start.isAfter(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        }

        return !now.isBefore(start) || !now.isAfter(end);
    }

    private synchronized PolicyConfig loadPolicyIfNeeded() throws IOException {
        PolicyConfig externalPolicy = loadPolicyFromExternalCandidates();
        if (externalPolicy != null) {
            return externalPolicy;
        }

        if (cachedPolicy != null) {
            return cachedPolicy;
        }

        return loadFromClasspath();
    }

    private PolicyConfig loadPolicyFromExternalCandidates() throws IOException {
        Path configuredPath = policyFilePath.isBlank() ? null : Path.of(policyFilePath);
        Path repoRootPath = Path.of("..", "policy.json").normalize();
        Path localPath = Path.of("policy.json").normalize();

        PolicyConfig externalPolicy = tryLoadExternal(configuredPath);
        if (externalPolicy != null) {
            return externalPolicy;
        }

        externalPolicy = tryLoadExternal(repoRootPath);
        if (externalPolicy != null) {
            return externalPolicy;
        }

        externalPolicy = tryLoadExternal(localPath);
        if (externalPolicy != null) {
            return externalPolicy;
        }

        return null;
    }

    private PolicyConfig loadFromClasspath() throws IOException {
        ClassPathResource resource = new ClassPathResource("policy.json");
        try (InputStream input = resource.getInputStream()) {
            cachedPolicy = objectMapper.readValue(input, PolicyConfig.class);
            cachedModifiedAt = -1L;
            return cachedPolicy;
        }
    }

    private PolicyConfig tryLoadExternal(final Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return null;
        }

        long modifiedAt = Files.getLastModifiedTime(path).toMillis();
        if (cachedPolicy != null && cachedModifiedAt == modifiedAt) {
            return cachedPolicy;
        }

        try (InputStream input = Files.newInputStream(path)) {
            cachedPolicy = objectMapper.readValue(input, PolicyConfig.class);
            cachedModifiedAt = modifiedAt;
            return cachedPolicy;
        }
    }
}
