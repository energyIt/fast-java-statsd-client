package com.energyit.statsd;

import java.nio.ByteBuffer;

public interface Sender {
    void send(ByteBuffer msg);
}