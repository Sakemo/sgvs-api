package com.flick.business.service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.flick.business.core.entity.security.User;

@Service
public class AuthenticatedUserService {

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return user;
    }

    public Long getAuthenticatedUserId() {
        User user = getAuthenticatedUser();
        return user.getId();
    }
}
