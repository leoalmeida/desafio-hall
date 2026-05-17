package com.example.backend.rest;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AuthRequestDto;
import com.example.backend.dto.AuthResponseDto;
import com.example.backend.security.AuditLogManager;
import com.example.backend.service.AuthService;

import jakarta.validation.Valid;

/**
 * Endpoint de autenticacao simples via token.
 */
@RestController
@CrossOrigin(origins = { "http://localhost:80", "http://localhost:4200" }, maxAge = RestConstants.CORS_MAX_AGE)
@RequestMapping("/api/auth")
public class AuthResource {

    private final AuthService service;
    private final AuditLogManager auditLogManager;

    public AuthResource(
            final AuthService service,
            final AuditLogManager auditLogManager) {
        this.service = Objects.requireNonNull(service, "service nao pode ser nulo");
        this.auditLogManager = Objects.requireNonNull(auditLogManager, "auditLogManager nao pode ser nulo");
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDto> login(
        @Valid @RequestBody final AuthRequestDto request) {

        this.auditLogManager.logAction(
                request.getEmail(), 
                "LOGIN",
                "AppUser", 
            request.getEmail(), 
                auditLogManager.toJsonNode(request));

        AuthResponseDto response = service.loginUser(request);

        return ResponseEntity.ok(response);
    }

}
