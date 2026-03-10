package com.flick.business.service.security;

import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.RegisterRequest;
import com.flick.business.core.entity.GeneralSettings;
import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.security.Role;
import com.flick.business.repository.GeneralSettingsRepository;
import com.flick.business.repository.security.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GeneralSettingsRepository generalSettingsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SessionRegistryService sessionRegistryService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_shouldCreateDefaultSettingsAndReturnToken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new-user")
                .email("new-user@example.com")
                .password("12345678")
                .build();

        User savedUser = User.builder()
                .id(10L)
                .username("new-user")
                .email("new-user@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("new-user")).thenReturn(false);
        when(userRepository.existsByEmail("new-user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(generalSettingsRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(generalSettingsRepository.save(any(GeneralSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRegistryService.rotateSession(10L)).thenReturn("session-id");
        when(jwtService.generateToken(eq(java.util.Map.of("sid", "session-id")), eq(savedUser))).thenReturn("jwt-token");

        AuthResponse response = authenticationService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getId()).isEqualTo(10L);
        verify(generalSettingsRepository).save(any(GeneralSettings.class));
    }
}
