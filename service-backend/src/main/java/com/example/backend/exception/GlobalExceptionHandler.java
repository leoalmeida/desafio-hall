package com.example.backend.exception;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/* Manipulador global de exceções para a aplicação. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Resource
    private MessageSource messageSource;

    /**
     * Método auxiliar para criar um mapa de cabeçalhos HTTP padrão.
     */
    private MultiValueMap<String, String> headers() {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        headers.forEach((key, value) -> multiValueMap.add(key, value));
        return multiValueMap;
    }

    /**
     * Método auxiliar para criar um objeto ResponseError a partir de uma mensagem e um
     *      código de status HTTP.
     */
    private ResponseError responseError(final String message, final HttpStatus statusCode) {
        ResponseError responseError = new ResponseError().setError(message).setStatusCode(statusCode.value());
        return responseError;
    }

    /**
     * Manipulador genérico para todas as exceções não tratadas especificamente por outros métodos.
     * Ele verifica o tipo da exceção e delega para o manipulador apropriado, ou retorna um erro
     *  genérico de servidor se a exceção não for reconhecida.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(@NonNull final Exception e, @NonNull final WebRequest request) {
        if (e.getClass().isAssignableFrom(UndeclaredThrowableException.class)) {
            assert e instanceof UndeclaredThrowableException : e.getClass();
            return handleBusinessException(
                    (BusinessException)
                            UndeclaredThrowableException.class.cast(e).getUndeclaredThrowable(),
                    request);
        } else if (e.getClass().isAssignableFrom(MethodArgumentNotValidException.class)) {
            assert e instanceof MethodArgumentNotValidException : e.getClass();
            ResponseError error = handleExceptionArgumentNotValid(MethodArgumentNotValidException.class.cast(e));
            return handleExceptionInternal(e, error, new HttpHeaders(headers()), HttpStatus.BAD_REQUEST, request);
        } else if (e.getClass().isAssignableFrom(EntityNotFoundException.class)) {
            assert e instanceof EntityNotFoundException : e.getClass();
            return handleEntityNotFoundException(EntityNotFoundException.class.cast(e), request);
        } else {
            String message = messageSource.getMessage(
                    "error.server", new Object[] {e.getMessage()}, Objects.requireNonNull(Locale.getDefault()));
            return handleExceptionInternal(
                    e,
                    responseError(message, HttpStatus.INTERNAL_SERVER_ERROR),
                    new HttpHeaders(headers()),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    request);
        }
    }

    /**
     * Manipulador específico para exceções de validação de argumentos, que coleta os erros de validação
     */
    private ResponseError handleExceptionArgumentNotValid(@NonNull final MethodArgumentNotValidException e) {
        Map<String, String> errors = new ConcurrentHashMap<>();
        ((MethodArgumentNotValidException) e)
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ResponseError error = responseError(errors.toString(), HttpStatus.BAD_REQUEST);
        return error;
    }

    /* Manipulador para exceções de entidade não encontrada, que retorna um erro 404 */
    @ExceptionHandler({EntityNotFoundException.class})
    ResponseEntity<Object> handleEntityNotFoundException(
            @NonNull final EntityNotFoundException e, @NonNull final WebRequest request) {
        ResponseError error = responseError(e.getMessage(), HttpStatus.NOT_FOUND);
        return handleExceptionInternal(e, error, new HttpHeaders(headers()), HttpStatus.NOT_FOUND, request);
    }

    /* Manipulador para exceções de negócio, que retorna um erro 422 */
    @ExceptionHandler({BusinessException.class})
    ResponseEntity<Object> handleBusinessException(
            @NonNull final BusinessException e, @NonNull final WebRequest request) {
        ResponseError error = responseError(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        return handleExceptionInternal(e, error, new HttpHeaders(headers()), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    /* Manipulador para exceções de argumento inválido, que retorna um erro 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Object> handleIllegalArgumentException(
            final IllegalArgumentException e, @NonNull final WebRequest request) {
        ResponseError error = responseError(e.getMessage(), HttpStatus.BAD_REQUEST);
        return handleExceptionInternal(e, error, new HttpHeaders(headers()), HttpStatus.BAD_REQUEST, request);
    }
}
