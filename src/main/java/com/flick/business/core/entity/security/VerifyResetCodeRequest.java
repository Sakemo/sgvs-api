package com.flick.business.core.entity.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyResetCodeRequest {
    @NotBlank(message = "E-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotBlank(message = "Código de verificação é obrigatório.")
    @Pattern(regexp = "^[0-9]{6}$", message = "Código de verificação inválido. Deve conter exatamente 6 dígitos.")
    private String verificationCode;
}
