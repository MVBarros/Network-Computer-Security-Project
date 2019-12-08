package tig.grpc.server.session;

import java.security.Key;
import java.time.LocalDateTime;

public class CustomUserToken {

    private Key sessionKey;
    private LocalDateTime expiration;

    public CustomUserToken(LocalDateTime expiration, Key sessionKey) {
        this.expiration = expiration;
        this.sessionKey = sessionKey;
    }

    public boolean authenticateToken() {
        return !expiration.isBefore(LocalDateTime.now());
    }


    public Key getSessionKey() {
        return sessionKey;
    }

    public LocalDateTime getExpiration() {
        return expiration;
    }
    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }


}
