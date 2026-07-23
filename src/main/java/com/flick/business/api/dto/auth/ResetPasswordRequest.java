package com.flick.business.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "Código de verificação é obrigatório.")
    @Pattern(regexp = "^[0-9]{6}$", message = "Código de verificação inválido. Deve conter exatamente 6 dígitos.")
    private String code;

    @NotBlank(message = "Nova senha é obrigatória.")
    @Size(min = 8, message = "A nova senha deve ter no mínimo 8 caracteres.")
    private String newPassword;
}
