package com.minimarket.security.service;

import com.minimarket.security.config.LoginAttemptProperties;
import com.minimarket.security.exception.AccountLockedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final LoginAttemptProperties properties;
    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(LoginAttemptProperties properties) {
        this.properties = properties;
    }

    public void checkNotBlocked(String username) {
        if (isBlocked(username)) {
            throw new AccountLockedException();
        }
    }

    public boolean isBlocked(String username) {
        AttemptRecord record = attempts.get(normalize(username));
        if (record == null) {
            return false;
        }
        if (record.lockedUntil != null && Instant.now().isBefore(record.lockedUntil)) {
            return true;
        }
        if (record.lockedUntil != null && Instant.now().isAfter(record.lockedUntil)) {
            attempts.remove(normalize(username));
        }
        return false;
    }

    public void loginFailed(String username) {
        String key = normalize(username);
        AttemptRecord record = attempts.computeIfAbsent(key, k -> new AttemptRecord());
        record.failedAttempts++;
        if (record.failedAttempts >= properties.getMaxAttempts()) {
            record.lockedUntil = Instant.now().plusSeconds(properties.getLockMinutes() * 60L);
        }
    }

    public void loginSucceeded(String username) {
        attempts.remove(normalize(username));
    }

    private String normalize(String username) {
        return username.toLowerCase();
    }

    private static class AttemptRecord {
        private int failedAttempts;
        private Instant lockedUntil;
    }
}
