package com.flick.business.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Usuário ou e-mail é obrigatório.")
    @Size(min = 3, max = 100, message = "Usuário ou e-mail deve ter entre 3 e 100 caracteres.")
    private String username;

    @NotBlank(message = "Senha é obrigatória.")
    @Size(max = 100, message = "Senha deve ter no máximo 100 caracteres.")
    private String password;
}
