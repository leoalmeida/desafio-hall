package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Application;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade Application.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Filtra aplicações por parte do nome usando filtro case-insensitive com LIKE.
     *
     * @param name Nome ou parte do nome da aplicação a ser pesquisada
     * @return Lista de aplicações que correspondem ao critério de busca
     */
    @Query("SELECT tab FROM Application tab WHERE LOWER(tab.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Application> searchByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Application b WHERE b.id = :id")
    Optional<Application> findByIdForUpdate(@Param("id") Long id);
}
