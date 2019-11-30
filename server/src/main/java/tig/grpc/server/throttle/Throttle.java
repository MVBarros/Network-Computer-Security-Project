package tig.grpc.server.throttle;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Throttle {
    public static ConcurrentHashMap<String, AtomicInteger> accessMap = new ConcurrentHashMap<>();

    public static void login(String username) {
        if (accessMap.contains(username)) {
            accessMap.get(username).addAndGet(1);
        }
        else
            accessMap.put(username, new AtomicInteger(1));
    }

    public static void throottle(String username) {
        try {
            TimeUnit.SECONDS.sleep(accessMap.get(username).intValue() * accessMap.get(username).intValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
