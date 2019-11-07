package tig.grpc.server.session;

import java.time.LocalDateTime;

public class TokenCleanupThread implements Runnable {
    public void run() {
        while (true) {
            for (String tokenKey : SessionAuthenticator.sessions.keySet()) {
                if (SessionAuthenticator.sessions.get(tokenKey).getExpiration().isBefore(LocalDateTime.now())) {
                    SessionAuthenticator.sessions.remove(tokenKey);
                }
            }
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
