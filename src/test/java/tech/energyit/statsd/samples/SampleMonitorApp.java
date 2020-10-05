package tech.energyit.statsd.samples;

import tech.energyit.statsd.FastStatsDClient;
import tech.energyit.statsd.StatsDClient;
import tech.energyit.statsd.SynchronousSender;
import tech.energyit.statsd.TagImpl;

public class SampleMonitorApp {

    private static final String STATSD_SERVER_HOST = "localhost";
    private static final int STATSD_SERVER_PORT = 8125;
    private static final byte[] ORDER_COUNT = "order.count".getBytes();
    private static final byte[] PROCESSING_TIME = "order.time".getBytes();
    private static final byte[] PRODUCT = "product".getBytes();

    private final SynchronousSender sender;
    private final StatsDClient client;

    public SampleMonitorApp() {
        // create a sender - provides lot's of options how to send data via UDP, but this is likely the best for you:
        sender = SynchronousSender.builder()
                .withHostAndPort(STATSD_SERVER_HOST, STATSD_SERVER_PORT)
                .build();
        client = new FastStatsDClient("tradeApp", sender);
    }

    public void close() {
        sender.close();
    }

    public void newOrders(String productName, long count, long durationInMs) {
        TagImpl productTag = new TagImpl(PRODUCT, productName.getBytes());
        client.count(ORDER_COUNT, count, productTag);
        client.time(PROCESSING_TIME, durationInMs, productTag);
    }

    public static void main(String[] args) {
        // init
        SampleMonitorApp monitor = new SampleMonitorApp();
        // start monitoring
        monitor.newOrders("product-A", 1, 10);
        monitor.newOrders("product-B", 3, 15);
        //...

        // stop app
        monitor.close();
    }

}
