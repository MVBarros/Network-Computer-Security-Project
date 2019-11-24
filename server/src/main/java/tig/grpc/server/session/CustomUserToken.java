package tig.grpc.server.session;

import java.security.Key;
import java.time.LocalDateTime;

public class CustomUserToken extends UserToken {

    private Key sessionKey;

    public CustomUserToken(LocalDateTime expiration, String username, Key sessionKey) {
        super(expiration, username);
        this.sessionKey = sessionKey;
    }

    public Key getSessionKey() {
        return sessionKey;
    }

}
