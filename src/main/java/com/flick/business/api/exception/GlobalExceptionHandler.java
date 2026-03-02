package com.flick.business.api.exception;

import com.flick.business.api.dto.response.common.ApiErrorResponse;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", ex.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), translateMessage(fieldError.getDefaultMessage()));
        }
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dados inválidos.", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dados inválidos.", null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String code = resolveCode(status, ex.getReason());
        String message = ex.getReason() != null ? ex.getReason() : defaultMessageByStatus(status);
        return buildError(status, code, message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Ocorreu um erro interno. Tente novamente.", null);
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            HttpStatus status,
            String code,
            String rawMessage,
            Map<String, String> errors) {

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                code,
                translateMessage(rawMessage),
                errors);
        return ResponseEntity.status(status).body(response);
    }

    private String resolveCode(HttpStatus status, String reason) {
        if (status == HttpStatus.TOO_MANY_REQUESTS
                && reason != null
                && reason.toLowerCase(Locale.ROOT).contains("too many login attempts")) {
            return "LOGIN_ATTEMPTS_EXCEEDED";
        }
        return switch (status) {
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case TOO_MANY_REQUESTS -> "TOO_MANY_REQUESTS";
            case BAD_REQUEST -> "BAD_REQUEST";
            case CONFLICT -> "CONFLICT";
            case NOT_FOUND -> "NOT_FOUND";
            default -> "HTTP_" + status.value();
        };
    }

    private String defaultMessageByStatus(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Não autorizado.";
            case FORBIDDEN -> "Acesso negado.";
            case TOO_MANY_REQUESTS -> "Muitas tentativas. Tente novamente mais tarde.";
            case BAD_REQUEST -> "Requisição inválida.";
            case NOT_FOUND -> "Recurso não encontrado.";
            default -> "Erro ao processar a requisição.";
        };
    }

    private String translateMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Erro ao processar a requisição.";
        }

        return switch (message) {
            case "Invalid credentials." -> "Credenciais inválidas.";
            case "Too many login attempts. Try again later." ->
                    "Muitas tentativas de login. Tente novamente mais tarde.";
            case "Username must have between 3 and 50 characters." ->
                    "Usuário deve ter entre 3 e 50 caracteres.";
            case "Username or email must have between 3 and 100 characters." ->
                    "Usuário ou e-mail deve ter entre 3 e 100 caracteres.";
            case "Email must be valid." -> "E-mail deve ser válido.";
            case "Password must have between 8 and 100 characters." ->
                    "Senha deve ter entre 8 e 100 caracteres.";
            case "Password must have at most 100 characters." ->
                    "Senha deve ter no máximo 100 caracteres.";
            case "A user with this username already exists." ->
                    "Já existe um usuário com este nome.";
            case "A user with this email already exists." ->
                    "Já existe um usuário com este e-mail.";
            case "Username or email already in use." ->
                    "Nome de usuário ou e-mail já está em uso.";
            case "Authenticated user not found." ->
                    "Usuário autenticado não encontrado.";
            case "Username is required" -> "Usuário é obrigatório.";
            case "Email is required" -> "E-mail é obrigatório.";
            case "Password is required" -> "Senha é obrigatória.";
            default -> translatePatternMessages(message);
        };
    }

    private String translatePatternMessages(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("must not be blank")) {
            return "Campo obrigatório.";
        }
        if (normalized.contains("must be a well-formed email address")) {
            return "E-mail deve ser válido.";
        }
        if (normalized.contains("size must be between")) {
            return "Tamanho inválido para o campo.";
        }
        return message;
    }
}
