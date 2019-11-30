package tig.grpc.server.throttle;

import java.util.concurrent.ConcurrentHashMap;

public class ThrottleThread implements Runnable {

    @Override
    public void run() {
        while(true) {
            Throttler.accessMap = new ConcurrentHashMap<>();
            try {
                Thread.sleep(1000 * 60 * 2);
            } catch (InterruptedException e) {
                //Should never happen
                e.printStackTrace();
            }
        }
    }
}
