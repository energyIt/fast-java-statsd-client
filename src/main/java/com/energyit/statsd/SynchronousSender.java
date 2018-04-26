package com.energyit.statsd;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;

class SynchronousSender implements Sender, Closeable {

    private static final StatsDClientErrorHandler NO_OP_HANDLER = new StatsDClientErrorHandler() {

        @Override
        public void handle(final Exception e) { /* No-op */ }

        @Override
        public void handle(String errorFormat, Object... args) { /* No-op */ }
    };

    private final Supplier<InetSocketAddress> addressLookup;

    private final DatagramChannel clientChannel;

    private final StatsDClientErrorHandler errorHandler;

    SynchronousSender(final String hostname, final int port) {
        this(hostname, port, NO_OP_HANDLER);
    }

    SynchronousSender(final String hostname, final int port, final StatsDClientErrorHandler errorHandler) {
        this(() -> new InetSocketAddress(inetAddress(hostname), port), errorHandler);
    }

    SynchronousSender(final Supplier<InetSocketAddress> addressLookup) {
        this(addressLookup, NO_OP_HANDLER);
    }

    SynchronousSender(final Supplier<InetSocketAddress> addressLookup, final StatsDClientErrorHandler errorHandler) {
        this.addressLookup = addressLookup;
        this.errorHandler = errorHandler;
        try {
            this.clientChannel = DatagramChannel.open();
            this.clientChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            this.clientChannel.connect(addressLookup.get());
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to start StatsD client", e);
        }
    }

    @Override
    public void send(ByteBuffer msg) {
        try {
            final int sizeOfBuffer = msg.limit();
            final int sentBytes = clientChannel.write(msg);
            if (sizeOfBuffer != sentBytes) {
                errorHandler.handle("Could not send entirely stat %s. Only sent %d bytes out of %d bytes",
                        msg.toString(), sentBytes, sizeOfBuffer);
            }
        } catch (IOException e) {
            errorHandler.handle(e);
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

    private static InetAddress inetAddress(String hostname) {
        try {
            return InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot create address", e);
        }
    }
}