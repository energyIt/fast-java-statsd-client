package tech.energyit.statsd;

import java.nio.ByteBuffer;

/**
 * Must deliver a message to target, e.g. {@link java.nio.channels.Channel}
 */
public interface Sender {
    void send(ByteBuffer msg);
}