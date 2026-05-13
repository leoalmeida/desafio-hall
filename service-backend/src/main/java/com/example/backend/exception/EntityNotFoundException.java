package com.example.backend.exception;

/* Exceção personalizada para erros de entidade não encontrada. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(final String entityKey, final String mensagem) {
        super(String.format("%s: %s", entityKey, mensagem));
    }

    public EntityNotFoundException(final String entityKey, final String mensagem, final Object... params) {
        super(String.format("%s: %s", entityKey, String.format(mensagem, params)));
    }
}
