package tech.energyit.statsd.async;

import tech.energyit.statsd.FastStatsDClient;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocateDirect;

class SenderEvent {

    private ByteBuffer buffer = allocateDirect(FastStatsDClient.INITIAL_BUFFER_SIZE);

    void set(ByteBuffer msg) {
        ByteBuffer eventBuffer = buffer;
        if (eventBuffer.capacity() < msg.limit()) {
            eventBuffer = allocateDirect(msg.capacity());
            this.buffer = eventBuffer;
        } else {
            eventBuffer.clear();
        }
        eventBuffer.put(msg);
        eventBuffer.flip();
    }

    ByteBuffer getMsg() {
        return this.buffer;
    }
}