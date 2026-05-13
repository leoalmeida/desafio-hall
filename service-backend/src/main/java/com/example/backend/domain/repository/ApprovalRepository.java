package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Approval;
import com.example.backend.domain.entity.OutcomeEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Approval.
 */
@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    /**
     * Busca aprovações por ID da release.
     *
     * @param releaseId ID da release
     * @return Lista de aprovações para a release
     */
    List<Approval> findByReleaseId(Long releaseId);

    /**
     * Busca aprovações por email do aprovador.
     *
     * @param approverEmail Email do aprovador
     * @return Lista de aprovações do aprovador
     */
    @Query("SELECT a FROM Approval a WHERE LOWER(a.approverEmail) = LOWER(:approverEmail)")
    List<Approval> findByApproverEmail(String approverEmail);

    /**
     * Busca aprovações por resultado (outcome).
     *
     * @param outcome Resultado da aprovação
     * @return Lista de aprovações com o resultado
     */
    @Query("SELECT a FROM Approval a WHERE a.outcome = :outcome")
    List<Approval> findByOutcome(OutcomeEnum outcome);
}
