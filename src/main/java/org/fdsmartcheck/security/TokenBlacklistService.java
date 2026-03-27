package org.fdsmartcheck.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // token -> expiration timestamp (ms)
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, long expirationMs) {
        blacklist.put(token, expirationMs);
        // Clean up expired tokens
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }

    public boolean isBlacklisted(String token) {
        Long expiration = blacklist.get(token);
        if (expiration == null) return false;
        if (expiration < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
