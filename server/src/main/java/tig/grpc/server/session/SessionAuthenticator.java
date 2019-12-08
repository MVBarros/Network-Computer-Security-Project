package tig.grpc.server.session;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class SessionAuthenticator {

    public static final ConcurrentHashMap<String, CustomUserToken> sessions = new ConcurrentHashMap<>();

    public static String insertCustomSession(String sessionId, Key sessionKey) {
        //Session Id valid for 5 minutes
        sessions.put(sessionId, new CustomUserToken(LocalDateTime.now().plusMinutes(5), sessionKey));
        return sessionId;
    }


    public static CustomUserToken authenticateSession(String sessionId) {

        if (!sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("Invalid SessionId");
        }

        CustomUserToken token = sessions.get(sessionId);

        if (!token.authenticateToken()) {
            sessions.remove(sessionId);
            throw new IllegalArgumentException("Session has expired");
        }

        //Update expiration if user authenticates successfully
        token.setExpiration(LocalDateTime.now().plusMinutes(5));

        return token;
    }

    public static void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

}
