package tig.grpc.backup.throttle;

import java.util.concurrent.atomic.AtomicInteger;

public class ThrottleThread implements Runnable {

    @Override
    public void run() {
        while(true) {
            Throttler.accessMap.replaceAll((k, v) -> new AtomicInteger(0));
            try {
                Thread.sleep(1000 * 60 * 60);
            } catch (InterruptedException e) {
                //Should never happen
                e.printStackTrace();
            }
        }
    }
}
