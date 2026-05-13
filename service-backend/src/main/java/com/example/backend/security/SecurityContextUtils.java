package com.example.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilitarios de acesso ao contexto de seguranca da requisicao atual.
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return "anonymous";
        }
        return authentication.getPrincipal().toString();
    }

    public static Authentication getAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }
        return context.getAuthentication();
    }

    public static boolean hasAuthentication() {
        return getAuthentication() != null;
    }

    public static void setAuthentication(final Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
