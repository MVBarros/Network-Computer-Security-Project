package tig.grpc.server.session;

import java.security.Key;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.HashSet;

public class CustomUserToken {

    private Key sessionKey;
    private LocalDateTime expiration;

    private PublicKey publicKey;

    public static final HashSet<String> nonces = new HashSet<String>();

    public CustomUserToken(LocalDateTime expiration, Key sessionKey, PublicKey publicKey) {
        this.expiration = expiration;
        this.sessionKey = sessionKey;
        this.publicKey = publicKey;
    }

    public boolean authenticateToken() {
        return !expiration.isBefore(LocalDateTime.now());
    }

    public boolean authenticateNonce(String nonce) {
        if (nonces.contains(nonce)) {
            throw new IllegalArgumentException("Repeated message");
        }
        nonces.add(nonce);
        return true;
    }


    public PublicKey getPublicKey() { return publicKey; }

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
