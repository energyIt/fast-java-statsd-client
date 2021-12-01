package tech.energyit.statsd.async;

import java.net.SocketException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import tech.energyit.statsd.FastStatsDClient;
import tech.energyit.statsd.StatsDClientErrorHandler;
import tech.energyit.statsd.Tag;
import tech.energyit.statsd.TagImpl;
import tech.energyit.statsd.utils.DummyStatsDServer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class AsyncStatsdIntegrationTest {

    private static final int STATSD_SERVER_PORT = 17254;
    private static AsynchronousSender sender;
    private static FastStatsDClient client;
    private static DummyStatsDServer server;

    @BeforeClass
    public static void start() throws SocketException {
        sender = AsynchronousSender.builder()
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
                }).build();
        client = new FastStatsDClient("my.prefix", sender, false);
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
        assertThat(server.messagesReceived()).containsExactly("my.prefix.my.metric:10|c|#" + tag1);
    }

}
