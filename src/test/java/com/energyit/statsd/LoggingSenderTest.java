package com.energyit.statsd;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.ByteBuffer;

@RunWith(MockitoJUnitRunner.class)
public class LoggingSenderTest {

    @Test
    public void sendShouldPrintMsg() {
        LoggingSender sender = new LoggingSender();
        String msg1 = "test-message-1";
        sender.send(ByteBuffer.wrap(msg1.getBytes()));
        String msg2 = "test-message-2";
        sender.send(ByteBuffer.wrap(msg2.getBytes()));
        Assertions.assertThat(sender.getMessages()).containsExactly(msg1,msg2);
    }
}
