package com.flick.business.api.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest (
  @Size(min=3, max=50) String username,
  @Email String email,
  @Size String password
){}
