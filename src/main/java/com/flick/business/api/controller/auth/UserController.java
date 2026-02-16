package com.flick.business.api.controller.auth;

import com.flick.business.api.dto.auth.UpdateUserRequest;
import com.flick.business.core.entity.security.User;
import com.flick.business.repository.security.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @jakarta.transaction.Transactional
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserRequest request) {

        if (request.username() != null) user.setUsername(request.username());
        if (request.email() != null) user.setEmail(request.email());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if(user != null) userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal User user) {
        if(user != null) userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
