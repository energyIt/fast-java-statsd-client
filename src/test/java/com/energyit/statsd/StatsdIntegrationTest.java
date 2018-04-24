package com.energyit.statsd;

import java.net.SocketException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author gregmil
 */
@RunWith(JUnit4.class)
public class StatsdIntegrationTest {

    private static final int STATSD_SERVER_PORT = 17254;
    private static SynchronousSender sender;
    private static FastStatsDClient client;
    private static DummyStatsDServer server;

    @BeforeClass
    public static void start() throws SocketException {
        sender = new SynchronousSender("localhost", STATSD_SERVER_PORT, new StatsDClientErrorHandler() {

            @Override
            public void handle(Exception exception) {
                exception.printStackTrace();
            }

            @Override
            public void handle(String errorFormat, Object... args) {
                System.out.format(errorFormat+"\n", args);
            }
        });
        client = new FastStatsDClient("my.prefix", sender);
        server = new DummyStatsDServer(STATSD_SERVER_PORT);
    }

    @AfterClass
    public static void stop() {
        server.close();
        sender.close();
    }

    @After
    public void clean() {
        server.clear();
    }
    @Test(timeout = 5000L)
    public void countWithOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        client.count("my.metric".getBytes(), 10, tag1);
        server.waitForMessage(1);
        assertThat(server.messagesReceived()).containsExactly("my.prefix.my.metric:10|c|#"+tag1);
    }

    @Test(timeout = 5000L)
    public void sendingWithNoServerListeningShouldNotBlock() {
        try (SynchronousSender sender = new SynchronousSender("localhost", STATSD_SERVER_PORT-1)) {
            FastStatsDClient client = new FastStatsDClient(sender);
            client.count("my.metric".getBytes(), -5);
        }
    }

    @Test(timeout = 5000L)
    public void sendingShouldBeQuick() {
        final Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        final byte[] bytes = "my.metric".getBytes();
        final int n = 1000;
        final long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            client.count(bytes, i, tag1);
        }
        System.out.println("Sending done in [ns] : " + (System.nanoTime()-start));
        server.waitForMessage(n-1);
        assertThat(server.messagesReceived()).contains("my.prefix.my.metric:10|c|#"+tag1);
        System.out.println("All done in [ns] : " + (System.nanoTime()-start));
    }
}
