package com.flick.business.service.security;

import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.LoginRequest;
import com.flick.business.api.dto.auth.RegisterRequest;
import com.flick.business.core.entity.security.User;
import com.flick.business.core.enums.security.Role;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final ConcurrentMap<String, LoginAttemptState> LOGIN_ATTEMPTS = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     *
     * @param request The registration request containing username and password.
     * @return An AuthResponse containing the JWT for the newly created user.
     */
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String rawPassword = request.getPassword();

        validateNormalizedRegistrationInput(username, email, rawPassword);

        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("A user with this username already exists.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        }

        var user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("Username or email already in use.");
        }

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .username(user.getUsername())
                .email(resolveEffectiveEmail(user))
                .role(user.getRole())
                .build();
    }

    /**
     * Authenticates an existing user.
     *
     * @param request The login request containing username and password.
     * @return An AuthResponse containing the JWT upon successful authentication.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsername().trim();
        String password = request.getPassword();
        validateNormalizedLoginInput(usernameOrEmail, password);
        checkRateLimit(usernameOrEmail);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            usernameOrEmail,
                            password));
        } catch (AuthenticationException ex) {
            registerFailedAttempt(usernameOrEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        var user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        applyLegacyIdentityFallbacks(user, usernameOrEmail);

        resetFailedAttempts(usernameOrEmail);
        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .id(user.getId())
                .username(resolveEffectiveUsername(user))
                .email(resolveEffectiveEmail(user))
                .role(user.getRole())
                .build();
    }

    private void validateNormalizedRegistrationInput(String username, String email, String password) {
        if (username.isBlank() || username.length() < 3 || username.length() > 50) {
            throw new BusinessException("Username must have between 3 and 50 characters.");
        }

        if (email.isBlank() || !SIMPLE_EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("Email must be valid.");
        }

        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new BusinessException("Password must have between 8 and 100 characters.");
        }
    }

    private void validateNormalizedLoginInput(String usernameOrEmail, String password) {
        if (usernameOrEmail.isBlank() || usernameOrEmail.length() < 3 || usernameOrEmail.length() > 100) {
            throw new BusinessException("Username or email must have between 3 and 100 characters.");
        }
        if (password == null || password.isBlank() || password.length() > 100) {
            throw new BusinessException("Password must have at most 100 characters.");
        }
    }

    private void checkRateLimit(String usernameOrEmail) {
        LoginAttemptState state = LOGIN_ATTEMPTS.get(usernameOrEmail);
        if (state == null) {
            return;
        }
        Instant now = Instant.now();
        if (state.lockedUntil != null && now.isBefore(state.lockedUntil)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many login attempts. Try again later.");
        }
    }

    private void registerFailedAttempt(String usernameOrEmail) {
        Instant now = Instant.now();
        LOGIN_ATTEMPTS.compute(usernameOrEmail, (key, current) -> {
            if (current == null || Duration.between(current.firstAttemptAt, now).compareTo(LOGIN_WINDOW) > 0) {
                return new LoginAttemptState(1, now, null);
            }

            int updatedAttempts = current.attempts + 1;
            Instant lockedUntil = updatedAttempts >= MAX_LOGIN_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
            return new LoginAttemptState(updatedAttempts, current.firstAttemptAt, lockedUntil);
        });
    }

    private void resetFailedAttempts(String usernameOrEmail) {
        LOGIN_ATTEMPTS.remove(usernameOrEmail);
    }

    private static final class LoginAttemptState {
        private final int attempts;
        private final Instant firstAttemptAt;
        private final Instant lockedUntil;

        private LoginAttemptState(int attempts, Instant firstAttemptAt, Instant lockedUntil) {
            this.attempts = attempts;
            this.firstAttemptAt = firstAttemptAt;
            this.lockedUntil = lockedUntil;
        }
    }

    private String resolveEffectiveEmail(User user) {
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        String username = resolveEffectiveUsername(user);
        if (username != null && SIMPLE_EMAIL_PATTERN.matcher(username).matches()) {
            return username;
        }
        return null;
    }

    private String resolveEffectiveUsername(User user) {
        String username = user.getUsername();
        if (username != null && !username.isBlank()) {
            return username;
        }
        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            int atIndex = email.indexOf('@');
            return atIndex > 0 ? email.substring(0, atIndex) : email;
        }
        return null;
    }

    private void applyLegacyIdentityFallbacks(User user, String identifier) {
        boolean changed = false;
        if ((user.getEmail() == null || user.getEmail().isBlank())
                && SIMPLE_EMAIL_PATTERN.matcher(identifier).matches()) {
            user.setEmail(identifier.toLowerCase());
            changed = true;
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            if (SIMPLE_EMAIL_PATTERN.matcher(identifier).matches()) {
                int atIndex = identifier.indexOf('@');
                user.setUsername(atIndex > 0 ? identifier.substring(0, atIndex) : identifier);
            } else {
                user.setUsername(identifier);
            }
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}
