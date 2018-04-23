package com.energyit.statsd;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class LoggingSender implements Sender {

    @Override
    public void send(ByteBuffer msg) throws IOException {
        System.out.println(new String(msg.array()));
    }
}
