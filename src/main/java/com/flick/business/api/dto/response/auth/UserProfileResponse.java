package com.flick.business.api.dto.response.auth;

import com.flick.business.core.enums.security.Role;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        Role role) {
}
