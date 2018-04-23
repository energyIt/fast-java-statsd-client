package com.energyit.statsd;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public interface Sender {
    void send(ByteBuffer msg) throws IOException;
}