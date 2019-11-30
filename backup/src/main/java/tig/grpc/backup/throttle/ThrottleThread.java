package tig.grpc.backup.throttle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThrottleThread implements Runnable {

    @Override
    public void run() {
        while(true) {
            Throttler.accessMap = new ConcurrentHashMap<>();
            try {
                Thread.sleep(1000 * 60 * 5);
            } catch (InterruptedException e) {
                //Should never happen
                e.printStackTrace();
            }
        }
    }
}
