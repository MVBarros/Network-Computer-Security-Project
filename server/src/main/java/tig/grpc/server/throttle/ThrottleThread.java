package tig.grpc.server.throttle;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThrottleThread implements Runnable {

    @Override
    public void run() {
        while(true) {
            Throttle.accessMap.replaceAll((k, v) -> new AtomicLong(0));
            try {
                Thread.sleep(1000 * 60 * 60);
            } catch (InterruptedException e) {
                //Should never happen
                e.printStackTrace();
            }
        }
    }
}
