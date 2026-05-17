package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Release;
import com.example.backend.domain.entity.EnvironmentEnum;
import com.example.backend.domain.entity.StatusEnum;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Release.
 */
@Repository
public interface ReleaseRepository extends JpaRepository<Release, UUID> {

    /**
     * Busca releases por applicationId, version, ambiente e status.
     *
     * @param applicationId ID da aplicação
     * @param version       Versão da release
     * @param env           Ambiente
     * @param status        Status
     * @return Lista de releases que correspondem aos critérios
     */
    @Query("SELECT r FROM Release r WHERE r.applicationId = :applicationId "
            + "AND r.version = :version "
            + "AND r.env = :env "
            + "AND r.status = :status")
        List<Release> findRelease(UUID applicationId, String version, EnvironmentEnum env, StatusEnum status);
}
