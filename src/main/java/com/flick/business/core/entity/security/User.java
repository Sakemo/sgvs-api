package com.flick.business.core.entity.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flick.business.core.enums.security.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(unique = true)
    private String googleId;

    @Column(name = "google_profile_picture")
    private String googleProfilePicture;

    @Column(name = "google_login_date")
    private LocalDateTime googleLoginDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Recover password
    @Column(name = "password_reset_code")
    private String passwordResetCode;

    @Column(name = "password_reset_code_expires_at")
    private LocalDateTime passwordResetCodeExpiresAt;

    @Column(name = "password_reset_attempts")
    private Integer passwordResetAttempts = 0;

    @Column(name = "password_reset_code_used")
    private Boolean passwordResetCodeUsed = false;

    @Column(name = "password_reset_requested_at")
    private LocalDateTime passwordResetRequestedAt;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
