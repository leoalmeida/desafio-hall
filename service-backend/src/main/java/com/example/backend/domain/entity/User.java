package com.example.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um release bancário.
 */
@Entity
@Table(name = "APP_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    static final long serialVersionUID = 1L;

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_ROLE_LENGTH = 80;
    private static final int MAX_TEXT_LENGTH = 255;

    @Id
    private String email;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = MAX_NAME_LENGTH)
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @NotBlank(message = "Perfil é obrigatório")
    @Size(max = MAX_ROLE_LENGTH)
    @Column(nullable = false, length = MAX_ROLE_LENGTH)
    private String role;

    @NotBlank(message = "Senha é obrigatória")
    @Size(max = MAX_TEXT_LENGTH)
    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String pawd;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = MAX_TEXT_LENGTH)
    @Column(nullable = false, length = MAX_TEXT_LENGTH)
    private String status;

    @Size(max = MAX_TEXT_LENGTH)
    @Column(length = MAX_TEXT_LENGTH)
    private String resetToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
