package com.flick.business.api.controller.auth;

import com.flick.business.api.dto.auth.UpdateUserRequest;
import com.flick.business.api.dto.response.auth.UserProfileResponse;
import com.flick.business.core.entity.security.User;
import com.flick.business.exception.BusinessException;
import com.flick.business.exception.ResourceAlreadyExistsException;
import com.flick.business.repository.security.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        User authenticatedUser = requireAuthenticatedUser(user);
        return ResponseEntity.ok(new UserProfileResponse(
                authenticatedUser.getId(),
                resolveEffectiveUsername(authenticatedUser),
                resolveEffectiveEmail(authenticatedUser),
                authenticatedUser.getRole()));
    }

    @PutMapping("/me")
    @jakarta.transaction.Transactional
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequest request) {
        User authenticatedUser = requireAuthenticatedUser(user);

        if (request.username() != null) {
            String normalizedUsername = request.username().trim();
            if (normalizedUsername.length() < 3 || normalizedUsername.length() > 50) {
                throw new BusinessException("Username must have between 3 and 50 characters.");
            }
            if (userRepository.existsByUsernameAndIdNot(normalizedUsername, authenticatedUser.getId())) {
                throw new ResourceAlreadyExistsException("A user with this username already exists.");
            }
            authenticatedUser.setUsername(normalizedUsername);
        }

        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (!SIMPLE_EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
                throw new BusinessException("Email must be valid.");
            }
            if (userRepository.existsByEmailAndIdNot(normalizedEmail, authenticatedUser.getId())) {
                throw new ResourceAlreadyExistsException("A user with this email already exists.");
            }
            authenticatedUser.setEmail(normalizedEmail);
        }

        if (request.password() != null && !request.password().isBlank()) {
            authenticatedUser.setPassword(passwordEncoder.encode(request.password()));
        }

        try {
            userRepository.save(authenticatedUser);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceAlreadyExistsException("Username or email already in use.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal User user) {
        User authenticatedUser = requireAuthenticatedUser(user);
        userRepository.delete(authenticatedUser);
        return ResponseEntity.noContent().build();
    }

    private User requireAuthenticatedUser(User user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found.");
        }
        return user;
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
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            int atIndex = user.getEmail().indexOf('@');
            return atIndex > 0 ? user.getEmail().substring(0, atIndex) : user.getEmail();
        }
        return null;
    }
}
