package com.energyit.statsd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.ByteBuffer;

@RunWith(MockitoJUnitRunner.class)
public class LoggingSenderTest {

    @Test
    public void sendShouldPrintMsg() throws IOException {
        new LoggingSender().send(ByteBuffer.wrap("test-message".getBytes()));
    }
}
