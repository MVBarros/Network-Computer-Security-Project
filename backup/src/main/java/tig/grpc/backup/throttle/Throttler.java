package tig.grpc.backup.throttle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Throttler {

    public static ConcurrentHashMap<String, Integer> accessMap = new ConcurrentHashMap<>();

    public static final int MAX_ATTEMPTS = 10;

    public static void access(String username) {
        if (accessMap.containsKey(username)) {
            accessMap.put(username, accessMap.get(username) + 1);
        }
        else {
            accessMap.put(username, 1);
        }
    }

    public static void throttle(String username) {
            if(accessMap.containsKey(username) && accessMap.get(username) >= MAX_ATTEMPTS) {
                throw new IllegalArgumentException("To much backups for this particular user");
            }
    }

}
