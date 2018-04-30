package com.energyit.statsd;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author gregmil
 */
public class DummyClient {

    private static final int STATSD_SERVER_PORT = 17254;

    public static void main(String[] args) {
        try (SynchronousSender sender = new SynchronousSender("localhost", STATSD_SERVER_PORT, new StatsDClientErrorHandler() {

            @Override
            public void handle(Exception exception) {
                exception.printStackTrace();
            }

            @Override
            public void handle(String errorFormat, Object... args) {
                System.out.format(errorFormat + "\n", args);
            }
        })) {
            FastStatsDClient client = new FastStatsDClient("my.prefix", sender);


            final Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
            final byte[] bytes = "my.metric".getBytes();
            final int n = 10000000;
            final long start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                client.count(bytes, i, tag1);
                LockSupport.parkNanos(100);
            }
            System.out.println("Sending done in [ms] : " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }
}
