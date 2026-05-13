package com.example.backend.exception;

/* Exceção personalizada para erros de autenticação. */
public class AuthException extends RuntimeException {
    public AuthException(final String mensagem) {
        super(mensagem);
    }

    public AuthException(final String mensagem, final Object... params) {
        super(String.format(mensagem, params));
    }
}
