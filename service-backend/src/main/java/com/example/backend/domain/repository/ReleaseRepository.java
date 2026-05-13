package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Release;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Release.
 */
@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

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
            + "AND LOWER(r.env) = LOWER(:env) "
            + "AND LOWER(r.status) = LOWER(:status)")
    List<Release> findRelease(Long applicationId, String version, String env, String status);
}
