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

        String exactTranslation = switch (message) {
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
            case "Invalid or expired JWT token" -> "Token JWT inválido ou expirado.";
            case "Customer is required for ON_CREDIT sales." -> "Cliente é obrigatório para vendas no fiado.";
            case "This customer is not enabled for credit purchases." -> "Este cliente não está habilitado para compras no fiado.";
            case "This customer is not enabled to puchase credits." -> "Este cliente não está habilitado para compras no fiado.";
            case "Cannot deactivate a customer with an outstanding debt balance." ->
                    "Não é possível desativar um cliente com saldo devedor pendente.";
            case "A customer with this Tax ID already exists in your account." ->
                    "Já existe um cliente com este CPF/CNPJ na sua conta.";
            case "Payment method cannot be ON_CREDIT" ->
                    "Forma de pagamento não pode ser fiado.";
            case "Unauthorized: One or more sales do not belong to your account." ->
                    "Não autorizado: uma ou mais vendas não pertencem à sua conta.";
            case "No sales found for the provided IDs." ->
                    "Nenhuma venda encontrada para os IDs informados.";
            case "Restocking expenses must contain at least one item." ->
                    "Despesas de reposição devem conter ao menos um item.";
            case "A value greater than zero is required for this type of expense." ->
                    "Um valor maior que zero é obrigatório para este tipo de despesa.";
            case "Restock items should not be provided for non-restocking expenses." ->
                    "Itens de reposição não devem ser informados para despesas que não são de reposição.";
            case "Unable to generate automatic data for restocking expense." ->
                    "Não foi possível gerar os dados automáticos da despesa de reposição.";
            case "Unable to generate automatic name for restocking expense." ->
                    "Não foi possível gerar o nome automático da despesa de reposição.";
            case "Unable to generate automatic description for restocking expense." ->
                    "Não foi possível gerar a descrição automática da despesa de reposição.";
            case "Cannot delete category as it is currently associated with existing sales." ->
                    "Não é possível excluir a categoria, pois ela está associada a vendas existentes.";
            default -> null;
        };

        if (exactTranslation != null) {
            return exactTranslation;
        }

        String dynamicTranslation = translateDynamicMessages(message);
        if (dynamicTranslation != null) {
            return dynamicTranslation;
        }

        return translatePatternMessages(message);
    }

    private String translateDynamicMessages(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);

        if (normalized.startsWith("product not found with id: ")) {
            return "Produto não encontrado com ID: " + message.substring("Product not found with ID: ".length());
        }
        if (normalized.startsWith("customer not found with id: ")) {
            return "Cliente não encontrado com ID: " + message.substring("Customer not found with ID: ".length());
        }
        if (normalized.startsWith("provider not found with id: ")) {
            return "Fornecedor não encontrado com ID: " + message.substring("Provider not found with ID: ".length());
        }
        if (normalized.startsWith("category not found with id: ")) {
            return "Categoria não encontrada com ID: " + message.substring("Category not found with ID: ".length());
        }
        if (normalized.startsWith("sale not found with id: ")) {
            return "Venda não encontrada com ID: " + message.substring("Sale not found with ID: ".length());
        }
        if (normalized.startsWith("expense not found with id: ")) {
            return "Despesa não encontrada com ID: " + message.substring("Expense not found with ID: ".length());
        }
        if (normalized.startsWith("invalid payment method: ")) {
            return "Forma de pagamento inválida: " + message.substring("Invalid payment method: ".length());
        }
        if (normalized.startsWith("invalid payment status: ")) {
            return "Status de pagamento inválido: " + message.substring("Invalid payment status: ".length());
        }
        if (normalized.startsWith("invalid expense type: ")) {
            return "Tipo de despesa inválido: " + message.substring("Invalid expense type: ".length());
        }
        if (normalized.startsWith("invalid groupby parameter.")) {
            return "Parâmetro groupBy inválido. Valores suportados: day, customer, paymentMethod.";
        }
        if (normalized.startsWith("credit limit exceeded for customer: ")) {
            return "Limite de crédito excedido para o cliente: " + message.substring("Credit limit exceeded for customer: ".length());
        }
        if (normalized.startsWith("sale with id ") && normalized.endsWith(" does not belong to the customer")) {
            return "Venda com ID " + message.substring("Sale with ID ".length(), message.length() - " does not belong to the customer".length())
                    + " não pertence ao cliente.";
        }
        if (normalized.startsWith("sale with id ") && normalized.endsWith(" is not pending payment")) {
            return "Venda com ID " + message.substring("Sale with ID ".length(), message.length() - " is not pending payment".length())
                    + " não está pendente de pagamento.";
        }
        if (normalized.startsWith("the amount paid ") && normalized.contains("does not match the total value of the selected sales")) {
            return "O valor pago não confere com o valor total das vendas selecionadas.";
        }
        if (normalized.startsWith("insufficient stock for product: ")) {
            int availableIndex = message.indexOf(". Available: ");
            int requestedIndex = message.indexOf(", Requested: ");
            if (availableIndex > 0 && requestedIndex > availableIndex) {
                String productName = message.substring("Insufficient stock for product: ".length(), availableIndex);
                String available = message.substring(availableIndex + ". Available: ".length(), requestedIndex);
                String requested = message.substring(requestedIndex + ", Requested: ".length());
                return "Estoque insuficiente para o produto: " + productName + ". Disponível: " + available
                        + ", Solicitado: " + requested + ".";
            }
            return "Estoque insuficiente para o produto informado.";
        }
        return null;
    }

    private String translatePatternMessages(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("must not be blank")) {
            return "Campo obrigatório.";
        }
        if (normalized.contains("must not be null")) {
            return "Campo obrigatório.";
        }
        if (normalized.contains("active is required")) {
            return "Status ativo é obrigatório.";
        }
        if (normalized.contains("must be a well-formed email address")) {
            return "E-mail deve ser válido.";
        }
        if (normalized.contains("size must be between")) {
            return "Tamanho inválido para o campo.";
        }
        if (normalized.contains("must be greater than or equal to")) {
            return "Valor inválido para o campo.";
        }
        return message;
    }
}
