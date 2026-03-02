package com.flick.business.api.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.LoginRequest;
import com.flick.business.api.exception.GlobalExceptionHandler;
import com.flick.business.service.security.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authenticationService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn429WithFriendlyBodyWhenLoginAttemptsExceeded() throws Exception {
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Too many login attempts. Try again later."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "john@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LOGIN_ATTEMPTS_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Muitas tentativas de login. Tente novamente mais tarde."));
    }

    @Test
    void shouldReturnTokenWhenLoginSucceeds() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("token")
                .id(1L)
                .username("john")
                .email("john@example.com")
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("john", "12345678"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }
}
