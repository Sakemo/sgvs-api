package com.flick.business.service.security;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class SessionRegistryService {

    private static final ConcurrentMap<Long, String> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    public String rotateSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        ACTIVE_SESSIONS.put(userId, sessionId);
        return sessionId;
    }

    public String getActiveSession(Long userId) {
        return ACTIVE_SESSIONS.get(userId);
    }

    public void registerSession(Long userId, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        ACTIVE_SESSIONS.putIfAbsent(userId, sessionId);
    }

    public void clearSession(Long userId) {
        ACTIVE_SESSIONS.remove(userId);
    }
}
