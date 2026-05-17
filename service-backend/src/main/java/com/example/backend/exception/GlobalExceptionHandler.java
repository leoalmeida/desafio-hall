package com.example.backend.exception;

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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;

/* Manipulador global de exceções para a aplicação. */
@RestControllerAdvice
@SuppressWarnings({"null", "unused"})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Resource
    private MessageSource messageSource;

    /**
     * Método auxiliar para criar um mapa de cabeçalhos HTTP padrão.
     */
    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Método auxiliar para criar um objeto ResponseError a partir de uma mensagem e um
     *      código de status HTTP.
     */
    private ResponseError responseError(final String code, final String message, final Object details) {
        return new ResponseError()
                .setCode(code)
                .setMessage(message)
                .setDetails(details);
    }

    private ResponseEntity<Object> buildResponse(
            final Exception e,
            final ResponseError error,
            final HttpStatus status,
            final WebRequest request) {
        return handleExceptionInternal(
                e,
                error,
                Objects.requireNonNull(headers()),
                status,
                Objects.requireNonNull(request));
    }

    /**
     * Manipulador genérico para todas as exceções não tratadas especificamente por outros métodos.
     * Ele verifica o tipo da exceção e delega para o manipulador apropriado, ou retorna um erro
     *  genérico de servidor se a exceção não for reconhecida.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(final Exception e, final WebRequest request) {
        if (e.getClass().isAssignableFrom(UndeclaredThrowableException.class)) {
            assert e instanceof UndeclaredThrowableException : e.getClass();
            return handleUndeclaredThrowableException(UndeclaredThrowableException.class.cast(e), request);
        } else if (e.getClass().isAssignableFrom(MethodArgumentNotValidException.class)) {
            assert e instanceof MethodArgumentNotValidException : e.getClass();
            ResponseError error = handleExceptionArgumentNotValid(MethodArgumentNotValidException.class.cast(e));
            return buildResponse(e, error, HttpStatus.BAD_REQUEST, request);
        } else if (e.getClass().isAssignableFrom(EntityNotFoundException.class)) {
            assert e instanceof EntityNotFoundException : e.getClass();
            return handleEntityNotFoundException(EntityNotFoundException.class.cast(e), request);
        } else if (e.getClass().isAssignableFrom(ObjectOptimisticLockingFailureException.class)) {
            assert e instanceof ObjectOptimisticLockingFailureException : e.getClass();
            return handleOptimisticLockingException(ObjectOptimisticLockingFailureException.class.cast(e), request);
        } else if (e.getClass().isAssignableFrom(ResponseStatusException.class)) {
            assert e instanceof ResponseStatusException : e.getClass();
            return handleResponseStatusException(ResponseStatusException.class.cast(e), request);
        } else {
            String message = messageSource.getMessage(
                    "error.server", new Object[] {e.getMessage()}, Objects.requireNonNull(Locale.getDefault()));
                return buildInternalErrorResponse(e, request, message);
        }
    }

            private ResponseEntity<Object> handleUndeclaredThrowableException(
                final UndeclaredThrowableException e, final WebRequest request) {
            return handleBusinessException((BusinessException) e.getUndeclaredThrowable(), request);
            }

            private ResponseEntity<Object> buildInternalErrorResponse(
                final Exception e, final WebRequest request, final String message) {
            return handleExceptionInternal(
                e,
                responseError("INTERNAL_ERROR", message, e.getClass().getSimpleName()),
                Objects.requireNonNull(headers()),
                HttpStatus.INTERNAL_SERVER_ERROR,
                Objects.requireNonNull(request));
            }

    /**
     * Manipulador específico para exceções de validação de argumentos, que coleta os erros de validação
     */
    private ResponseError handleExceptionArgumentNotValid(final MethodArgumentNotValidException e) {
        Map<String, String> errors = new ConcurrentHashMap<>();
        ((MethodArgumentNotValidException) e)
                .getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ResponseError error = responseError("VALIDATION_ERROR", "Invalid request fields", errors);
        return error;
    }

    /* Manipulador para exceções de entidade não encontrada, que retorna um erro 404 */
    @ExceptionHandler({EntityNotFoundException.class})
    ResponseEntity<Object> handleEntityNotFoundException(
            final EntityNotFoundException e, final WebRequest request) {
        ResponseError error = responseError("NOT_FOUND", e.getMessage(), "Entity not found");
        return buildResponse(e, error, HttpStatus.NOT_FOUND, request);
    }

    /* Manipulador para exceções de negócio, que retorna um erro 422 */
    @ExceptionHandler({BusinessException.class})
    ResponseEntity<Object> handleBusinessException(
            final BusinessException e, final WebRequest request) {
        ResponseError error = responseError("BUSINESS_ERROR", e.getMessage(), "Business validation failed");
        return buildResponse(e, error, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class})
    ResponseEntity<Object> handleOptimisticLockingException(
            final ObjectOptimisticLockingFailureException e, final WebRequest request) {
        ResponseError error = responseError(
                "CONFLICT",
                "Release atualizada concorrentemente. Recarregue o estado e tente novamente.",
                "Optimistic locking conflict");
        return buildResponse(e, error, HttpStatus.CONFLICT, request);
    }

    /* Manipulador para exceções de argumento inválido, que retorna um erro 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    @SuppressWarnings("unused")
    ResponseEntity<Object> handleIllegalArgumentException(
            final IllegalArgumentException e, final WebRequest request) {
        ResponseError error = responseError("INVALID_ARGUMENT", e.getMessage(), "Illegal argument");
        return buildResponse(e, error, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Object> handleResponseStatusException(
            final ResponseStatusException e, final WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String code = switch (status) {
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case BAD_REQUEST -> "BAD_REQUEST";
            case CONFLICT -> "CONFLICT";
            default -> "HTTP_" + status.value();
        };
        String reasonPhrase = status.getReasonPhrase();
        String message = e.getReason() == null ? reasonPhrase : e.getReason();
        ResponseError error = responseError(code, message, reasonPhrase);
        return buildResponse(e, error, status, request);
    }
}
