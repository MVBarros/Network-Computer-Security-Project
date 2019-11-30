package tig.grpc.server.throttle;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Throttler {
    public static ConcurrentHashMap<String, Integer> accessMap = new ConcurrentHashMap<>();
    public static final int MAX_ATTEMPTS = 3;
    public static void failure(String username) {
        if (accessMap.containsKey(username)) {
            accessMap.put(username, accessMap.get(username) + 1);
        } else
            accessMap.put(username, 1);
    }

    public static void success(String username) {
        accessMap.remove(username);
    }

    public static void throttle(String username) {
            if (accessMap.containsKey(username) &&  accessMap.get(username) >= MAX_ATTEMPTS) {
                throw new IllegalArgumentException("3 bad Requests for login with that user in a row, try again later (max 2 minutes)");
            }
    }
}
