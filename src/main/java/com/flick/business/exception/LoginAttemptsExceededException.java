package com.flick.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

public class LoginAttemptsExceededException extends ResponseStatusException {

    private final Instant lockedUntil;

    public LoginAttemptsExceededException(Instant lockedUntil) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Try again later.");
        this.lockedUntil = lockedUntil;
    }

    public long getRetryAfterSeconds(Instant now) {
        long seconds = lockedUntil.getEpochSecond() - now.getEpochSecond();
        return Math.max(seconds, 0L);
    }
}
