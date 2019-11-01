package tig.grpc.server.session;

import tig.grpc.server.utils.StringGenerator;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class SessionAuthenticator {

    private static ConcurrentHashMap<String, UserToken> sessions = new ConcurrentHashMap<String, UserToken>();

    public synchronized static String createSession(String username) {
        String sessionId;
        do {
            sessionId = StringGenerator.randomString(256);
        } while (sessions.containsKey(sessionId));
        //Session Id valid for 5 minutes
        sessions.put(sessionId, new UserToken(LocalDateTime.now().plusMinutes(5), username));
        return sessionId;
    }


    public synchronized static String authenticateSession(String sessionId) {

        if (!sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("Invalid SessionId");
        }

        UserToken token = sessions.get(sessionId);

        if (token.getExpiration().isBefore(LocalDateTime.now())) {
            sessions.remove(sessionId);
            throw new IllegalArgumentException("Session has expired");
        }

        //Update expiration if user authenticates successfully
        token.setExpiration(LocalDateTime.now().plusMinutes(5));
        return token.getUsername();
    }

    public static void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
