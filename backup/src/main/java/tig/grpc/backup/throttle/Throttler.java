package tig.grpc.backup.throttle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Throttler {

    public static ConcurrentHashMap<String, AtomicInteger> accessMap = new ConcurrentHashMap<>();

    public static void access(String username) {
        if (accessMap.containsKey(username)) {
            accessMap.get(username).incrementAndGet();
        }
        else {
            accessMap.put(username, new AtomicInteger(1));
        }
    }

    public static void throttle(String username) {
        try {
            TimeUnit.SECONDS.sleep((int)Math.pow(accessMap.get(username).get(), 2));
        } catch (InterruptedException e) {
            //Should never happen
            e.printStackTrace();
        }
    }

}
