package com.flick.business.api.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest (
  @Size(min = 3, max = 50, message = "Usuário deve ter entre 3 e 50 caracteres.") String username,
  @Email(message = "E-mail deve ser válido.") String email,
  @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres.") String password
){}
