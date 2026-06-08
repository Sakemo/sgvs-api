package com.flick.business.api.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flick.business.api.dto.auth.AuthResponse;
import com.flick.business.api.dto.auth.LoginRequest;
import com.flick.business.api.dto.auth.RegisterRequest;
import com.flick.business.api.dto.auth.GoogleLoginRequest;
import com.flick.business.service.security.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    /**
        Endpoint for user registration.
        @param request The registration data
        @return A response containint the JWT for the new user
    */
   @PostMapping("/register")
   public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authenticationService.register(request));
   }

   /**
    * Enpoint for user login
    * @param request The login data
    * @return A response containing the JWT upon successful authentication
    */
   @PostMapping("/login")
   public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
       return ResponseEntity.ok(authenticationService.login(request));
   }

   /**
    * Endpoint for Google OAuth login
    * @param request The Google login data containing the ID token
    * @return A response containing the JWT upon successful authentication
    */
   @PostMapping("/google")
   public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
       AuthResponse response = authenticationService.googleLogin(request.getIdToken());
       return ResponseEntity.ok(response);
   }
   
}