package com.energyit.statsd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class FastStatsDClientTest {

    private LoggingSender sender;
    private FastStatsDClient statsDClient;

    @Before
    public void setUp() {
        sender= new LoggingSender();
        statsDClient = new FastStatsDClient("my.prefix", sender);
    }

    @Test
    public void countShouldBeSendCorrectly() {
        statsDClient.count("my.metric".getBytes(), 10);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c");
    }

    @Test
    public void countWithRateShouldBeSendCorrectly() {
        statsDClient.count("my.metric".getBytes(), 10, 0.1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|@0.1");
    }

    @Test
    public void countWithOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.count("my.metric".getBytes(), 10, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|#"+tag1);
    }

    @Test
    public void countWithRateAndOneTagShouldBeSendCorrectly() {
        Tag tag1 = new TagImpl("tag1".getBytes(), "val1".getBytes());
        statsDClient.count("my.metric".getBytes(), 10, 0.1, tag1);
        assertThat(sender.getMessages()).containsExactly("my.prefix.my.metric:10|c|@0.1|#"+tag1);
    }
}
