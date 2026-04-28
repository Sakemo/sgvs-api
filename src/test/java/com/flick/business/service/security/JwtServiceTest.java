package com.flick.business.service.security;

import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final long JWT_EXPIRATION = 86_400_000L;

    @Test
    void shouldGenerateAndReadTokenUsingRawSecret() {
        JwtService jwtService = buildJwtService("a-long-random-secret-with-at-least-32-characters");
        User user = buildUser();

        String token = jwtService.generateToken(Map.of("sid", "sid-1"), user);

        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void shouldGenerateAndReadTokenUsingBase64Secret() {
        String rawSecret = "a-long-random-secret-with-at-least-32-characters";
        String base64Secret = Base64.getEncoder().encodeToString(rawSecret.getBytes());
        JwtService jwtService = buildJwtService(base64Secret);
        User user = buildUser();

        String token = jwtService.generateToken(Map.of("sid", "sid-1"), user);

        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    private JwtService buildJwtService(String secretKey) {
        JwtService jwtService = new JwtService(new SessionRegistryService());
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        return jwtService;
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .username("john")
                .password("encoded")
                .email("john@example.com")
                .role(Role.USER)
                .build();
    }
}
