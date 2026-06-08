package com.flick.business.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para login via Google
 */
public class GoogleLoginRequest {
    
    @NotBlank(message = "ID token é obrigatório")
    private String idToken;

    public GoogleLoginRequest() {
    }

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
