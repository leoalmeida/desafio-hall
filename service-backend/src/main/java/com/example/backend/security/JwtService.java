package com.example.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Servico simples para emissao e validacao de JWT.
 */
@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String EMAIL_CLAIM = "email";
    private static final String NAME_CLAIM = "name";

    private final byte[] secretBytes;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") final String secret,
            @Value("${security.jwt.expiration-ms:3600000}") final long expirationMs) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
    }

    public String generateToken(final String email, final String name, final String role) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMs);


        return Jwts.builder()
                .subject(email) 
                .claim(NAME_CLAIM, name)
                .claim(ROLE_CLAIM, role)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String extractEmail(final String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(final String token) {
        Object role = parseClaims(token).get(ROLE_CLAIM);
        return role == null ? "" : role.toString();
    }

    public boolean isTokenValid(final String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.after(new Date());
    }
}
