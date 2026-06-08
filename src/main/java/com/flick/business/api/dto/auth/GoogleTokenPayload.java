package com.flick.business.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload extraído do ID token validado do Google
 */
public class GoogleTokenPayload {
    
    @JsonProperty("sub")
    private String googleId;
    
    private String email;
    private String name;
    private String picture;
    
    @JsonProperty("email_verified")
    private boolean emailVerified;
    
    private long iat;
    private long exp;
    private String aud;
    
    // Construtores
    public GoogleTokenPayload() {
    }

    public GoogleTokenPayload(String googleId, String email, String name, String picture) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }

    // Getters e Setters
    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }
}
