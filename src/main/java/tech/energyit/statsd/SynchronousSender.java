package tech.energyit.statsd;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Supplier;

/**
 * Writes a message to a {@link DatagramChannel}.
 */
public class SynchronousSender implements Sender, Closeable {

    private final DatagramChannel clientChannel;

    private final StatsDClientErrorHandler errorHandler;

    private SynchronousSender(final Supplier<DatagramChannel> socketSupplier, final Supplier<InetSocketAddress> addressLookup, final StatsDClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        try {
            this.clientChannel = socketSupplier.get();
            this.clientChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            this.clientChannel.connect(addressLookup.get());
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to connect channel", e);
        }
    }

    @Override
    public void send(ByteBuffer msg) {
        try {
            final int sizeOfBuffer = msg.limit();
            final int sentBytes = clientChannel.write(msg);
            if (sizeOfBuffer != sentBytes) {
                errorHandler.handle("Could not send complete message : %s. %d/%d bytes sent.",
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
            } catch (final Exception e) {
                errorHandler.handle(e);
            }
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Supplier<DatagramChannel> socketSupplier = IOUtils::newDatagramChannel;
        private Supplier<InetSocketAddress> addressLookup = () -> new InetSocketAddress(IOUtils.inetAddress("localhost"), 8125);
        private StatsDClientErrorHandler errorHandler = StatsDClientErrorHandler.NO_OP_HANDLER;

        public Builder withHostAndPort(String hostname, int port) {
            addressLookup = () -> new InetSocketAddress(IOUtils.inetAddress(hostname), port);
            return this;
        }

        public Builder withSocketSupplier(Supplier<DatagramChannel> socketSupplier) {
            this.socketSupplier = socketSupplier;
            return this;
        }

        public Builder withAddressLookup(Supplier<InetSocketAddress> addressLookup) {
            this.addressLookup = addressLookup;
            return this;
        }

        public Builder withErrorHandler(StatsDClientErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public SynchronousSender build() {
            return new SynchronousSender(socketSupplier, addressLookup, errorHandler);
        }
    }

}