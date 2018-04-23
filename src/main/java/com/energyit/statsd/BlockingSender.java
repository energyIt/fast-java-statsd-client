package com.energyit.statsd;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;

class BlockingSender implements Sender , Closeable{
    private static final int PACKET_SIZE_BYTES = 1400;

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {
        @Override
        public void handle(final Exception e) { /* No-op */ }

        @Override
        public void error(String error) { /* No-op */ }
    };


    private final ByteBuffer sendBuffer = ByteBuffer.allocateDirect(PACKET_SIZE_BYTES);


    private final Supplier<InetSocketAddress> addressLookup;

    private final DatagramChannel clientChannel;

    private final StatsDClientErrorHandler errorHandler = NO_OP_HANDLER;

    BlockingSender(final Supplier<InetSocketAddress> addressLookup) {
        this.addressLookup = addressLookup;

        try {
            clientChannel = DatagramChannel.open();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to start StatsD client", e);
        }
    }

    @Override
    public void send(ByteBuffer msg) throws IOException {


        final InetSocketAddress address = addressLookup.get();

        final int sizeOfBuffer = sendBuffer.position();
        sendBuffer.flip();

        final int sentBytes = clientChannel.send(sendBuffer, address);
        sendBuffer.limit(sendBuffer.capacity());
        sendBuffer.rewind();

        if (sizeOfBuffer != sentBytes) {
            errorHandler.error(String.format(
                    "Could not send entirely stat %s to host %s:%d. Only sent %d bytes out of %d bytes",
                    sendBuffer.toString(),
                    address.getHostName(),
                    address.getPort(),
                    sentBytes,
                    sizeOfBuffer));
        }
    }

    /**
     * Cleanly shut down this StatsD client. This method may throw an exception if
     * the socket cannot be closed.
     */
    @Override
    public void close() {
        if (clientChannel != null) {
            try {
                clientChannel.close();
            } catch (final IOException e) {
                errorHandler.handle(e);
            }
        }

    }
}