package tech.energyit.statsd.async;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import tech.energyit.statsd.IOUtils;
import tech.energyit.statsd.Sender;
import tech.energyit.statsd.StatsDClientErrorHandler;
import tech.energyit.statsd.SynchronousSender;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Uses {@link Disruptor} to invoke the {@link SynchronousSender} in a dedicated thread.
 * If the ringbuffer is full, it either drops the message or uses {@link SynchronousSender}
 * to publish messages in the calling thread - configurable in builder.
 */
public class AsynchronousSender implements Sender, Closeable {

    private static final int RINGBUFFER_SIZE = 256;

    private final Disruptor<SenderEvent> disruptor;
    private final SynchronousSender sender;
    private final StatsDClientErrorHandler errorHandler;
    private final BiConsumer<AsynchronousSender, ByteBuffer> ringBufferFullHandler;

    private AsynchronousSender(final Supplier<DatagramChannel> socketSupplier,
                               final Supplier<InetSocketAddress> addressLookup,
                               final StatsDClientErrorHandler errorHandler,
                               final int ringbufferSize,
                               final BiConsumer<AsynchronousSender, ByteBuffer> ringBufferFullHandler) {
        this.disruptor = new Disruptor<>(new SenderEventFactory(), ringbufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());
        this.sender = SynchronousSender.builder()
                .withSocketSupplier(socketSupplier)
                .withAddressLookup(addressLookup)
                .withErrorHandler(errorHandler)
                .build();
        this.disruptor.handleEventsWith(new SenderEventHandler(sender));
        this.disruptor.start();
        this.ringBufferFullHandler = ringBufferFullHandler;
        this.errorHandler = errorHandler;
    }

    @Override
    public void send(final ByteBuffer msg) {
        if (!disruptor.getRingBuffer().tryPublishEvent((senderEvent, seq, message) -> senderEvent.set(message), msg)) {
            ringBufferFullHandler.accept(this, msg);
        }
    }

    @Override
    public void close() {
        disruptor.shutdown();
        sender.close();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static final BiConsumer<AsynchronousSender, ByteBuffer> SKIPPING_HANDLER = (s, m) -> s.errorHandler.handle("Ringbuffer full. Skipping...");
        private Supplier<DatagramChannel> socketSupplier = IOUtils::newDatagramChannel;
        private Supplier<InetSocketAddress> addressLookup = () -> new InetSocketAddress(IOUtils.inetAddress("localhost"), 8125);
        private StatsDClientErrorHandler errorHandler = StatsDClientErrorHandler.NO_OP_HANDLER;
        private int ringbufferSize = RINGBUFFER_SIZE;
        private BiConsumer<AsynchronousSender, ByteBuffer> ringBufferFullHandler = SKIPPING_HANDLER;

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

        public Builder withRingbufferSize(int ringbufferSize) {
            this.ringbufferSize = ringbufferSize;
            return this;
        }

        public Builder skipMessageWhenRingbufferIsFull() {
            this.ringBufferFullHandler = SKIPPING_HANDLER;
            return this;
        }

        public Builder publishSynchronouslyWhenRingbufferIsFull() {
            this.ringBufferFullHandler = (s, m) -> s.sender.send(m);
            return this;
        }

        public AsynchronousSender build() {
            return new AsynchronousSender(socketSupplier, addressLookup, errorHandler, ringbufferSize, ringBufferFullHandler);
        }
    }

}