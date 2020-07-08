package tech.energyit.statsd.utils;

import tech.energyit.statsd.*;

import java.util.concurrent.TimeUnit;

/**
 * @author gregmil
 */
public class DummyClientRunner {

    private static final int STATSD_SERVER_PORT = 17254;

    public static void main(String[] args) {
        try (SynchronousSender sender = SynchronousSender.builder()
                .withHostAndPort("localhost", STATSD_SERVER_PORT)
                .withErrorHandler(new StatsDClientErrorHandler() {
                    @Override
                    public void handle(Exception exception) {
                        exception.printStackTrace();
                    }

                    @Override
                    public void handle(String errorFormat, Object... args) {
                        System.out.format(errorFormat + "\n", args);
                    }
                }).build()) {
            FastStatsDClient client = new FastStatsDClient("my.prefix", sender, false);


            final Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
            final byte[] bytes = "my.metric".getBytes();
            final int n = 1000000;
            final long start = System.nanoTime();
            for (int i = 0; i < n; i++) {
                client.count(bytes, i, tag1);
            }
            System.out.println("Sending done in [ms] : " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        }
    }
}
