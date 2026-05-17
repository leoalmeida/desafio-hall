package com.example.backend.domain.repository;

import com.example.backend.domain.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade AuditLog.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Busca logs de auditoria por ator.
     *
     * @param actor Nome do ator
     * @return Lista de logs da auditoria para o ator
     */
    @Query("SELECT al FROM AuditLog al WHERE LOWER(al.actor) LIKE LOWER(CONCAT('%', :actor, '%'))")
    List<AuditLog> findByActorContainingIgnoreCase(@Param("actor") String actor);

    /**
     * Busca logs de auditoria por ação.
     *
     * @param action Ação realizada
     * @return Lista de logs com a ação
     */
    @Query("SELECT al FROM AuditLog al WHERE LOWER(al.action) LIKE LOWER(CONCAT('%', :action, '%'))")
    List<AuditLog> findByActionContainingIgnoreCase(@Param("action") String action);

    /**
     * Busca logs de auditoria por entidade.
     *
     * @param entity Nome da entidade
     * @return Lista de logs para a entidade
     */
    List<AuditLog> findByEntity(String entity);

    /**
     * Busca logs de auditoria por intervalo de datas.
     *
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de logs no intervalo
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
