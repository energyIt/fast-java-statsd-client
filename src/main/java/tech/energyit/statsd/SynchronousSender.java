package tech.energyit.statsd;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;

public class SynchronousSender implements Sender, Closeable {

    private final DatagramChannel clientChannel;

    private final StatsDClientErrorHandler errorHandler;

    public SynchronousSender(final String hostname, final int port) {
        this(hostname, port, StatsDClientErrorHandler.NO_OP_HANDLER);
    }

    public SynchronousSender(final String hostname, final int port, final StatsDClientErrorHandler errorHandler) {
        this(() -> new InetSocketAddress(IOUtils.inetAddress(hostname), port), errorHandler);
    }

    public SynchronousSender(final Supplier<InetSocketAddress> addressLookup, final StatsDClientErrorHandler errorHandler) {
        this(IOUtils::newDatagramChannel, addressLookup, errorHandler);
    }

    public SynchronousSender(final Supplier<DatagramChannel> socketSupplier, final Supplier<InetSocketAddress> addressLookup, final StatsDClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        try {
            this.clientChannel = socketSupplier.get();
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


}